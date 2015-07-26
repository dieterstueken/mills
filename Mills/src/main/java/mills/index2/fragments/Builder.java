package mills.index2.fragments;

import mills.bits.PGroup;
import mills.bits.Pattern;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

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
                List<RecursiveTask<Partition>> tasks = AbstractRandomList.generate(100, i -> {
                    PopCount pop = PopCount.get(i);
                    return partition(pop);
                });

                invokeAll(tasks);

                List<Partition> partitions = AbstractRandomList.map(tasks, ForkJoinTask::join);
                return new Partitions(partitions);
            }
        };
    }

    public RecursiveTask<Partition> partition(PopCount pop) {

        return new RecursiveTask<Partition>() {

            @Override
            protected Partition compute() {
                EntryTable source = RingEntry.MINIMIZED.filter(pop.eq);

                Set<PGroup> groups = PGroup.groups(source);
                final List<RecursiveTask<Radials>> taskset = new ArrayList<>(1<<groups.size());

                final byte idx[] = new byte[128];

                // back to forth
                for(int mlt=127; mlt>=0; --mlt) {

                    int index = PGroup.pindex(groups, mlt);
                    if(index==mlt) {
                        RecursiveTask<Radials> task = radials(source, index);
                        idx[mlt] = (byte) taskset.size();
                        taskset.add(task);
                    } else {
                        idx[mlt] = idx[index];
                    }
                }

                ForkJoinTask.invokeAll(taskset);

                List<Radials> fragset = AbstractRandomList.map(taskset, ForkJoinTask::join);
                List<Radials> fragments = AbstractRandomList.generate(128, i -> taskset.get(idx[i]).join());

                return new Partition(source, fragset, fragments);
            }
        };
    }

    RecursiveTask<Radials> radials(EntryTable source, int mlt) {
        return new RecursiveTask<Radials>() {
            @Override
            protected Radials compute() {

                EntryTable filtered = tables.table(source.filter(filters.get(mlt)));

                if(filtered.isEmpty())
                    return Radials.EMPTY;

                return new FragmentsBuilder(filtered).build();
            }
        };
    }

    class FragmentsBuilder {

        final EntryTable filtered;

        Pattern blacks = Pattern.NONE;
        Pattern whites = Pattern.NONE;

        final List<RecursiveTask<Fragments>> tasks = new ArrayList<>(81);
        final List<RecursiveTask<Fragments>> taskset = new ArrayList<>();

        public FragmentsBuilder(EntryTable filtered) {
            this.filtered = filtered;

            for (RingEntry e : filtered) {
                blacks = blacks.or(e.b);
                whites = whites.or(e.w);
            }
        }

        // return all stones which have any overlap
        RingEntry clip(RingEntry radial) {
            return RingEntry.of(
                    blacks.and(radial.b),
                    whites.and(radial.w)
            );
        }

        RecursiveTask<Fragments> task(RingEntry radial) {
            RingEntry clipped = clip(radial);
            if(clipped.equals(radial)) {
                RecursiveTask<Fragments> task = fragments(filtered, radial);
                taskset.add(task);
                return task;
            } else {
                // substitute previously prepared task
                return tasks.get(clipped.radix());
            }
        }

        Radials build() {

            for (RingEntry radial : RingEntry.RADIALS) {
                tasks.add(task(radial));
            }

            ForkJoinTask.invokeAll(taskset);

            List<Fragments> fragset = AbstractRandomList.map(taskset, ForkJoinTask::join);
            List<Fragments> fragments = AbstractRandomList.map(tasks, ForkJoinTask::join);

            switch(fragset.size()) {
                case 1: fragsets_1.incrementAndGet(); break;
                case 2: fragsets_2.incrementAndGet(); break;
                default: fragsets_n.incrementAndGet(); break;
            }

            return new Radials(filtered, fragset, fragments);
        }
    }

    final AtomicInteger fragsets_1 = new AtomicInteger();
    final AtomicInteger fragsets_2 = new AtomicInteger();
    final AtomicInteger fragsets_n = new AtomicInteger();

    RecursiveTask<Fragments> fragments(EntryTable source, RingEntry radials) {

        return new RecursiveTask<Fragments>() {

            PopCount clop(RingEntry e) {
                return e.clop().add(e.radials().and(radials).pop());
            }

            @Override
            protected Fragments compute() {

                if(source.isEmpty())
                    return Fragments.EMPTY;

                if (source.size() == 1) {
                    fragset_1.incrementAndGet();
                    RingEntry e = source.get(0);
                    return Fragments.of(clop(e), source);
                }

                int msk = 0;

                for (RingEntry e : source) {
                    final byte index = clop(e).index;

                    // may look like 8 mill candidates
                    if(index<25)
                        msk |= (1<<index);
                }

                int count = Integer.bitCount(msk);

                if (count == 1) {
                    fragset_1.incrementAndGet();
                    RingEntry e = source.get(0);
                    PopCount clop = e.clop().add(e.radials().and(radials).pop());
                    return Fragments.of(clop, source);
                }

                byte clops[] = new byte[count];
                List<EntryTable> frags = new ArrayList<>(count);

                //List<PopCount> cloplist = AbstractRandomList.virtual(source, this::clop);

                for (PopCount clop : PopCount.CLOSED) {
                    if ((msk & (1<<clop.index)) != 0) {
                        int i = frags.size();
                        clops[i] = clop.index;
                        final EntryTable fragment = source.filter(e -> clop(e).equals(clop));

                        assert !fragment.isEmpty();

                        frags.add(fragment);
                        clops[i] = clop.index;
                    }
                }

                switch(count) {
                    case 1: fragset_1.incrementAndGet(); break;
                    case 2: fragset_2.incrementAndGet(); break;
                    default: fragset_n.incrementAndGet(); break;
                }

                return Fragments.of(clops, msk, tables.register(frags));
            }
        };
    }

    final AtomicInteger fragset_1 = new AtomicInteger();
    final AtomicInteger fragset_2 = new AtomicInteger();
    final AtomicInteger fragset_n = new AtomicInteger();

    public static void main(String ... args) {

        for(int n=0; n<1000; ++n) {

            Builder b = new Builder(new EntryTables());

            Partitions p = b.partitions().invoke();

            //b.tables.stat(System.out);
            System.out.format("run %d %d \n", b.tables.count(), n);
        }
    }
}
