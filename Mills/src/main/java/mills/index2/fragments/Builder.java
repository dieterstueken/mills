package mills.index2.fragments;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
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
                    EntryTable filtered = RingEntry.MINIMIZED.filter(pop.eq);
                    return partition(filtered);
                });

                invokeAll(tasks);

                List<Partition> partitions = AbstractRandomList.map(tasks, ForkJoinTask::join);
                return new Partitions(partitions);
            }
        };
    }

    public RecursiveTask<Partition> partition(EntryTable source) {

        return new RecursiveTask<Partition>() {

            @Override
            protected Partition compute() {
                Set<PGroup> groups = PGroup.groups(source);
                final List<RecursiveTask<Fragments>> tasks = new ArrayList<>(1<<groups.size());

                final byte idx[] = new byte[128];

                // back to forth
                for(int mlt=127; mlt>=0; --mlt) {

                    int index = PGroup.pindex(groups, mlt);
                    if(index==mlt) {
                        RecursiveTask<Fragments> task = fragments(source, index);
                        idx[mlt] = (byte) tasks.size();
                        tasks.add(task);
                    } else {
                        idx[mlt] = idx[index];
                    }
                }

                ForkJoinTask.invokeAll(tasks);

                List<Fragments> fragset = AbstractRandomList.map(tasks, ForkJoinTask::join);
                List<Fragments> fragments = AbstractRandomList.generate(128, i -> tasks.get(idx[i]).join());

                return new Partition(source, fragset, fragments);
            }
        };
    }

    RecursiveTask<Fragments> fragments(EntryTable source, int mlt) {
        return new RecursiveTask<Fragments>() {
            @Override
            protected Fragments compute() {
                EntryTable filtered = tables.table(source.filter(filters.get(mlt)));
                if(filtered.isEmpty())
                    return Fragments.EMPTY;

                List<RecursiveTask<Fragment>> tasks = AbstractRandomArray.map(PopCount.CLOSED, p -> fragment(filtered, p));
                ForkJoinTask.invokeAll(tasks);
                List<Fragment> fragments = AbstractRandomArray.map(tasks, ForkJoinTask::join);
                return new Fragments(source, fragments);
            }
        };
    }

    RecursiveTask<Fragment> fragment(EntryTable source, PopCount clop) {

        return new RecursiveTask<Fragment>() {

            @Override
            protected Fragment compute() {

                 EntryTable filtered = source.filter(this::test);
                 if(filtered.isEmpty())
                     return Fragment.EMPTY;

                Map<EntryTable, List<RingEntry>> fragments = new TreeMap<>(EntryTable.BY_ORDER);

                for (RingEntry rad : RingEntry.RADIALS) {
                    EntryTable frag = filtered.filter(e->testRad(e, rad));
                    if (!frag.isEmpty()) {
                        fragments.computeIfAbsent(frag, key->new ArrayList<>()).add(rad);
                    }
                }

                return buildFragment(fragments);
            }

            boolean testRad(RingEntry e, RingEntry rad) {
                return e.radials().and(rad).pop().add(e.pop).equals(clop);
            }

            boolean test(RingEntry e) {

                // range of possible clops
                PopCount min = e.clop();
                PopCount max = min.add((e.radials().pop));

                return min.le(clop) && clop.le(max);
            }
        };
    }

    private Fragment buildFragment(Map<EntryTable, List<RingEntry>> fmap) {
        List<EntryTable> fragments = new ArrayList<>(fmap.size());
        List<List<RingEntry>> rads = new ArrayList<>(fmap.size());

        for (Map.Entry<EntryTable, List<RingEntry>> entry : fmap.entrySet()) {
            fragments.add(entry.getKey());
            rads.add(entry.getValue());
        }

        return new Fragment(tables.register(fragments), tables.register(rads));
    }

    public static void main(String ... args) {

        Builder b = new Builder(new EntryTables());

        Partitions p = b.partitions().invoke();

        System.out.println(p.toString());
    }
}
