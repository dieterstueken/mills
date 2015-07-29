package mills.index2.fragments;

import mills.bits.PGroup;
import mills.bits.Pattern;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.IntStream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:41
 * modified by: $Author$
 * modified on: $Date$
 */
public class Builder {

    final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

    final EntryTables tables;

    public Builder(EntryTables tables) {
        this.tables = tables;
    }

    public RecursiveTask<Partitions> partitions() {
        return new RecursiveTask<Partitions>() {

            @Override
            protected Partitions compute() {
                List<RecursiveTask<MaskTable>> tasks = AbstractRandomList.generate(100, i -> {
                    PopCount pop = PopCount.get(i);
                    return maskTask(pop);
                });

                invokeAll(tasks);
                List<MaskTable> partitions = AbstractRandomList.map(tasks, ForkJoinTask::join);
                return new Partitions(RingEntry.MINIMIZED, partitions);
            }
        };
    }

    public RecursiveTask<MaskTable> maskTask(PopCount pop) {

        return new RecursiveTask<MaskTable>() {

            @Override
            protected MaskTable compute() {
                EntryTable root = RingEntry.MINIMIZED.filter(pop.eq);

                if(root.isEmpty())
                    return MaskTable.EMPTY;

                Set<PGroup> groups = PGroup.groups(root);
                final List<RecursiveTask<RadialTable>> taskset = new ArrayList<>(1<<groups.size());
                final List<RecursiveTask<RadialTable>> tasks = new ArrayList<>(128);

                IntStream.range(0, 128).mapToObj(index -> {

                    assert index == tasks.size();

                    int lindex = PGroup.lindex(groups, index);
                    if (lindex < index) {
                        return tasks.get(lindex);
                    } else {
                        RecursiveTask<RadialTable> task = radialTask(root, lindex);
                        taskset.add(task);
                        return task;
                    }
                }).forEach(tasks::add);

                // invoke all but drop empty Radials
                ForkJoinTask.invokeAll(taskset);
                taskset.removeIf(task -> task.join().root.isEmpty());

                assert !taskset.isEmpty();

                return maskTable(pop, root,
                        AbstractRandomList.map(tasks, ForkJoinTask::join),
                        AbstractRandomList.map(taskset, ForkJoinTask::join));
            }
        };
    }

    static MaskTable maskTable(PopCount pop, EntryTable root, List<RadialTable> fragments, List<RadialTable> fragset) {
        return new MaskTable(root) {

            @Override
            public RadialTable get(int index) {
                return fragments.get(index);
            }

            @Override
            public Collection<RadialTable> content() {
                return fragset;
            }

            public PopCount pop() {
                return pop;
            }
        };
    }

    static UnaryOperator<RingEntry> clip(Pattern blacks, Pattern whites) {
        return entry -> RingEntry.of(blacks.and(entry.b), whites.and(entry.w));
    }

    static UnaryOperator<RingEntry> clip(EntryTable table) {

        Pattern blacks = Pattern.NONE;
        Pattern whites = Pattern.NONE;

        for (RingEntry e : table) {

            e = e.radials(); // radials only

            blacks = blacks.or(e.b);
            whites = whites.or(e.w);
        }

        return clip(blacks, whites);
    }

    RecursiveTask<RadialTable> radialTask(EntryTable source, int mlt) {

        return new RecursiveTask<RadialTable>() {

            @Override
            protected RadialTable compute() {
                EntryTable root = tables.table(source.filter(filters.get(mlt)));

                if(root.isEmpty())
                    return RadialTable.EMPTY;

                final List<RecursiveTask<ClopTable>> taskset = new ArrayList<>();
                final List<RecursiveTask<ClopTable>> tasks = new ArrayList<>(81);

                RingEntry.RADIALS.stream().map(clip(root)).map(radials -> {
                    int radix = radials.radix();
                    int size = tasks.size();
                    if (radix < size) {
                        return tasks.get(radix);
                    } else {
                        assert radix == size;
                        RecursiveTask<ClopTable> task = clopTask(root, radials);
                        taskset.add(task);
                        return task;
                    }
                }).forEach(tasks::add);

                ForkJoinTask.invokeAll(taskset);

                List<ClopTable> fragset = AbstractRandomList.map(taskset, ForkJoinTask::join);
                List<ClopTable> tables = AbstractRandomList.map(tasks, ForkJoinTask::join);

                return radialTable(root, tables, fragset);
            }
        };
    }

    static RadialTable radialTable(EntryTable root, List<ClopTable> tables, List<ClopTable> fragset) {
        return new RadialTable(root) {

            @Override
            public ClopTable get(int index) {
                return tables.get(index);
            }

            @Override
            public Collection<ClopTable> content() {
                return fragset;
            }
        };
    }

    static ClopTable singletonClop(PopCount clop, EntryTable root) {

        return new ClopTable(root) {

            @Override
            public EntryTable get(int index) {
                return index==clop.index ? root : EntryTable.EMPTY;
            }

            @Override
            public Collection<EntryTable> content() {
                return Collections.singleton(root);
            }
        };
    }

    static ClopTable indexClop(EntryTable root, byte clops[], List<EntryTable> tables) {

        return new ClopTable(root) {
            @Override
            public EntryTable get(int index) {

                int i = Arrays.binarySearch(clops, (byte) index);
                return i<0 ? EntryTable.EMPTY : tables.get(i);
            }

            @Override
            public Collection<EntryTable> content() {
                return tables;
            }
        };
    }

    RecursiveTask<ClopTable> clopTask(EntryTable root, RingEntry radials) {

        return new RecursiveTask<ClopTable>() {

            PopCount clop(RingEntry e) {
                return e.clop().add(e.radials().and(radials).pop());
            }

            @Override
            protected ClopTable compute() {

                if(root.isEmpty())
                    return ClopTable.EMPTY;

                if (root.size() == 1) {
                    RingEntry e = root.get(0);
                    return singletonClop(clop(e), root);
                }

                int msk = 0;

                for (RingEntry e : root) {
                    final byte index = clop(e).index;

                    // may look like 8 mill candidates
                    if(index<25)
                        msk |= (1<<index);
                }

                int count = Integer.bitCount(msk);

                if (count == 1) {
                    RingEntry e = root.get(0);
                    PopCount clop = e.clop().add(e.radials().and(radials).pop());
                    return singletonClop(clop, root);
                }

                byte clops[] = new byte[count];
                List<EntryTable> frags = new ArrayList<>(count);

                //List<PopCount> cloplist = AbstractRandomList.virtual(root, this::clop);

                for (PopCount clop : PopCount.CLOSED) {
                    if ((msk & (1<<clop.index)) != 0) {
                        int i = frags.size();
                        clops[i] = clop.index;
                        final EntryTable fragment = root.filter(e -> clop(e).equals(clop));

                        assert !fragment.isEmpty();

                        frags.add(fragment);
                        clops[i] = clop.index;
                    }
                }

                return indexClop(root, clops, tables.register(frags));
            }
        };
    }

    public static void main(String ... args) {

        Builder b = new Builder(new EntryTables());

        Partitions p = b.partitions().invoke();

        int n_rads=0, n_msk=0, n_clops=0, n_tables=0;

        for (MaskTable partition : p.content()) {

            if(!partition.content().isEmpty())
                ++n_msk;

            for (RadialTable radialTable : partition.content()) {
                if(!radialTable.content().isEmpty())
                    ++n_rads;

                for (ClopTable clopTable : radialTable.content()) {
                    if(!clopTable.content().isEmpty())
                        ++n_clops;

                    n_tables += clopTable.content().size();
                }
            }
        }

        //b.tables.stat(System.out);
        System.out.format("run msk=%d rad=%d clop=%d tbl=%d all=%d\n", n_msk, n_rads, n_clops, n_tables, b.tables.count());

        p.size();
    }
}
