package mills.index.partitions;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  05.07.12 13:54
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * Class PartitionTables provides a list of 100 PartitionTables for each PopCount.
 */
public class PartitionTables extends AbstractRandomList<PartitionTable> {

    public static final int SIZE = PopCount.TABLE.size();

    // pre calculated tables of entries for given PopCounts[00-99]
    protected final PartitionTable partitions[] = new PartitionTable[SIZE];

    protected PartitionTables() {
    }

    protected PartitionTables(final List<? extends PartitionTable> tables) {
        tables.toArray(partitions);
    }

    public int size() {
        return SIZE;
    }

    /**
     * Return MskTable for a given pop count.
     *
     * @param pop count of the requested MskTable.
     * @return a MskTable for a given pop count.
     */
    public PartitionTable get(int pop) {
        return partitions[pop];
    }

    public PartitionTable get(PopCount pop) {
        return partitions[pop.index()];
    }

    /////////////////////////////////////////////////////////////////////////////////////

    public static class Builder extends RecursiveTask<PartitionTables> {

        public Builder() {
            for(PopCount pop:PopCount.TABLE)
                tasks.add(task(pop));
        }

        final List<ForkJoinTask<PartitionTable>> tasks = new ArrayList<>(SIZE);

        ForkJoinTask<PartitionTable> task(final PopCount pop) {
            return new RecursiveTask<PartitionTable>() {

                String name = "todo";

                public String toString() {
                    return String.format("task[%d%d] %s", pop.nb, pop.nw, name);
                }

                @Override
                protected PartitionTable compute() {
                    name = Thread.currentThread().getName();
                    final EntryTable entries = RingEntry.MINIMIZED.filter(pop.eq);

                    if(pop.equals(pop.of(8,0)))
                        pop.hashCode();

                    return PartitionTable.build(entries);
                }
            };
        }

        @Override
        protected PartitionTables compute() {

            invokeAll(tasks);

            List<PartitionTable> tables = Lists.transform(tasks,
                    new Function<ForkJoinTask<PartitionTable>, PartitionTable>() {
                        @Override
                        public PartitionTable apply(ForkJoinTask<PartitionTable> task) {
                            return task.join();
                        }
                    });

            return new PartitionTables(tables);
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////

    private void dump() {

        System.out.println("partition tables");

        Stat stat = new Stat();

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final PartitionTable t = get(pop.index());
                int n = t.tables.size();
                int l = t.get(0).size();
                System.out.format("%5d:%2d", l,n);

                t.forEach(_p->stat.accept(_p.size()));
            }
            System.out.println();
        }

        stat.dump("total");
    }

    public static void main(String... args) throws InterruptedException, ExecutionException {
        ForkJoinPool.commonPool().invoke(new Builder()).dump();
    }
}
