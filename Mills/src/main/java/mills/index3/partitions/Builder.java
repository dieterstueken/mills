package mills.index3.partitions;

import mills.bits.PGroup;
import mills.bits.Pattern;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:41
 * modified by: $Author$
 * modified on: $Date$
 */
public class Builder extends RecursiveTask<Partitions> {

    final EntryTables registry;

    public Builder(EntryTables registry) {
        this.registry = registry;
    }

    public Partitions compute() {
        ForkJoinTask<List<EntryTable>> lePopTask = submit(this::lePop);

        List<MaskTable> partitions = computeAll(PopCount.TABLE, this::maskTable);

        List<MaskTable> content = partitions.stream().filter(t->!t.content().isEmpty()).collect(Collectors.toList());

        //System.out.format("radials: %d clops: %d\n", radials.get(), clops.get());

        return new Partitions(partitions, lePopTask.join()) {
            @Override
            public List<MaskTable> content() {
                return content;
            }
        };
    }

    static <T> ForkJoinTask<T> submit(Callable<T> compute) {
        return ForkJoinTask.adapt(compute).fork();
    }

    static <T> List<? extends ForkJoinTask<T>> waitAll(List<? extends ForkJoinTask<T>> tasks) {

        // use reverse order
        for(int i=tasks.size(); i>0; --i)
            tasks.get(i-1).join();

        return tasks;
    }

    static <T> List<T> joinAll(List<? extends ForkJoinTask<T>> tasks) {
        return AbstractRandomList.map(tasks, ForkJoinTask::join);
    }

    static <T,R> List<R> computeAll(Collection<T> src, Function<? super T, R> compute) {

        return joinAll(src.stream().
                map(t -> submit(() -> compute.apply(t))).
                collect(Collectors.toList()));
    }

    List<EntryTable> lePop() {
        return computeAll(PopCount.TABLE, pop -> RingEntry.TABLE.filter(pop.le));
    }

    MaskTable maskTable(PopCount pop) {
        EntryTable root = RingEntry.MINIMIZED.filter(pop.eq);

        if(root.isEmpty())
            return MaskTable.EMPTY;

        Set<PGroup> groups = PGroup.groups(root);
        final List<ForkJoinTask<RadialTable>> taskset = new ArrayList<>(1<<groups.size());
        final List<ForkJoinTask<RadialTable>> tasks = new ArrayList<>(128);

        IntStream.range(0, 128).mapToObj(index -> {

            assert index == tasks.size();

            int lindex = PGroup.lindex(groups, index);
            if (lindex < index) {
                return tasks.get(lindex);
            } else {
                ForkJoinTask<RadialTable> task = submit(() -> radialTable(root, index));
                taskset.add(submit(() -> radialTable(root, index)));
                return task;
            }
        }).forEach(tasks::add);

        // invoke all and drop empty Radials
        waitAll(taskset).removeIf(task -> task.join().root.isEmpty());

        assert !taskset.isEmpty();

        return maskTable(pop, root, joinAll(tasks), joinAll(taskset));
    }

    static MaskTable maskTable(PopCount pop, EntryTable root, List<RadialTable> fragments, List<RadialTable> fragset) {

        return new MaskTable(root) {

            @Override
            public RadialTable get(int index) {
                return fragments.get(index);
            }

            @Override
            public List<RadialTable> content() {
                return fragset;
            }

            public PopCount pop() {
                return pop;
            }
        };
    }

    // clip off any radials not set
    static UnaryOperator<RingEntry> clip(Pattern blacks, Pattern whites) {
        return entry -> RingEntry.of(blacks.and(entry.b), whites.and(entry.w));
    }

    // create clip operator from given list of entries
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

    final List<Predicate<RingEntry>> filters = AbstractRandomList.generate(128, msk -> e -> e.stable(2 * msk));

    RadialTable radialTable(EntryTable source, int mlt) {
        EntryTable root = registry.table(source.filter(filters.get(mlt)));

        if(root.isEmpty())
            return RadialTable.EMPTY;

        final List<ForkJoinTask<ClopTable>> taskset = new ArrayList<>();
        final List<ForkJoinTask<ClopTable>> tasks = new ArrayList<>(81);

        RingEntry.RADIALS.stream().map(clip(root)).map(radials -> {
            int radix = radials.radix();
            int size = tasks.size();
            if (radix < size) {
                return tasks.get(radix);
            } else {
                assert radix == size;
                ForkJoinTask<ClopTable> task = submit(() -> clopTable(root, radials));
                taskset.add(task);
                return task;
            }
        }).forEach(tasks::add);

        assert !taskset.isEmpty();

        List<ClopTable> fragset = joinAll(taskset);
        List<ClopTable> tables = joinAll(tasks);

        //radials.incrementAndGet();

        return radialTable(root, tables, fragset);
    }

    //final AtomicInteger radials = new AtomicInteger();
    //final AtomicInteger clops = new AtomicInteger();

    static RadialTable radialTable(EntryTable root, List<ClopTable> tables, List<ClopTable> fragset) {
        return new RadialTable(root) {

            @Override
            public ClopTable get(int index) {
                return tables.get(index);
            }

            @Override
            public List<ClopTable> content() {
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
            public List<EntryTable> content() {
                return Collections.singletonList(root);
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
            public List<EntryTable> content() {
                return tables;
            }
        };
    }

    static PopCount clop(RingEntry radials, RingEntry e) {
        return e.clop().add(e.radials().and(radials).pop());
    }

    ClopTable clopTable(EntryTable root, RingEntry radials) {

        if(root.isEmpty())
            return ClopTable.EMPTY;

        //clops.incrementAndGet();

        if (root.size() == 1) {
            RingEntry e = root.get(0);
            return singletonClop(clop(radials, e), root);
        }

        int msk = 0;

        for (RingEntry e : root) {
            final byte index = clop(radials, e).index;

            // may look like 8 mill candidates
            if(index<25)
                msk |= (1<<index);
        }

        int count = Integer.bitCount(msk);

        if (count == 1) {
            RingEntry e = root.get(0);
            return singletonClop(clop(radials, e), root);
        }

        byte clops[] = new byte[count];
        List<EntryTable> frags = new ArrayList<>(count);

        for (PopCount pop : PopCount.CLOSED) {
            if ((msk & (1<<pop.index)) != 0) {
                int i = frags.size();
                clops[i] = pop.index;
                final EntryTable fragment = root.filter(e -> clop(radials, e).equals(pop));

                assert !fragment.isEmpty();

                frags.add(fragment);
                clops[i] = pop.index;
            }
        }

        return indexClop(root, clops, registry.register(frags));
    }

    public static void main(String ... args) {

        Builder b = new Builder(new EntryTables());

        Partitions p = b.invoke();

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

        //b.registry.stat(System.out);
        System.out.format("run msk=%d rad=%d clop=%d tbl=%d all=%d\n", n_msk, n_rads, n_clops, n_tables, b.registry.count());

        p.size();
    }
}
