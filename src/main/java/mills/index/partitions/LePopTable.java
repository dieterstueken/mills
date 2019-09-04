package mills.index.partitions;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.10.11
 * Time: 17:57
 */

/**
 * A virtual List of EntryTables.
 * Each entry is generated recursively on demand by calling get().
 * <p/>
 * Even if the list is immutable it should be copied in advance to speed up access.
 */
public class LePopTable extends AbstractRandomList<EntryTable> {

    public static final LePopTable INSTANCE = new LePopTable();

    public static LePopTable open() {
        INSTANCE.size();
        return INSTANCE;
    }

    public static final int SIZE = PopCount.TABLE.size();

    @Override
    public int size() {
        return SIZE;
    }

    public EntryTable get(PopCount pop) {
        return pop==null ? EntryTable.EMPTY : get(pop.index());
    }

    private List<EntryTable> table = new AbstractRandomList<EntryTable>() {

        final AtomicBoolean started = new AtomicBoolean(false);

        final Builder builder = new Builder() {

            // reset self if ready
            @Override
            protected List<EntryTable> compute() {
                List<EntryTable> result = super.compute();
                table = result;
                return result;
            }
        };

        @Override
        public int size() {
            if(!started.getAndSet(true))
                builder.fork();
            return SIZE;
        }

        @Override
        public EntryTable get(int index) {
            if(!started.getAndSet(true))
                builder.fork();

            return builder.tasks.get(index).join();
        }
    };

    @Override
    public EntryTable get(int index) {
        return table.get(index);
    }

    ///////////////////////////////////////////////////////////////////////////

    private static class Builder extends RecursiveTask<List<EntryTable>> {

        final List<ForkJoinTask<EntryTable>> tasks = new ArrayList<>(SIZE);

        EntryTable upTable(final PopCount pop) {

            // may be incremented
            if(pop.min()<8) {
                // increment the bigger one if < 9 else the other one
                Player up =  pop.nw<pop.nb && pop.nw<9 ? Player.White : Player.Black;
                return tasks.get(pop.add(up.pop).index).join();
            }

            // 8:8 and above: full table
            return RingEntry.TABLE;
        }

        ForkJoinTask<EntryTable> task(final PopCount pop) {

            return new RecursiveTask<EntryTable>() {

                String name = "todo";

                public String toString() {
                    return String.format("task[%d%d] %s", pop.nb, pop.nw, name);
                }

                @Override
                protected EntryTable compute() {
                    name = Thread.currentThread().getName();
                    //names.add(toString());
                    EntryTable upTable = upTable(pop);
                    return upTable.filter(ple(pop));
                }
            };
        }

        /**
         * Generate filter Predicate for elements with le pop count.
         * A RingEntry holds maximum 8 stones.
         *
         * @return a filter predicate.
         */
        public static Predicate<RingEntry> ple(PopCount pop) {

            if (pop.min() < 8)
                return e -> e != null && e.pop.le(pop);
            else
                return EntryTable.ALL;
        }

        public Builder() {
            for(PopCount pop:PopCount.TABLE)
                tasks.add(task(pop));
        }

        @Override
        protected List<EntryTable> compute() {

            invokeAll(tasks);

            return List.copyOf(AbstractRandomList.transform(tasks, ForkJoinTask::join));
        }
    }

    ///////////////////////////////////////////////////////////////////////////

    private void dump() {

        System.out.println("leTable");

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                final PopCount pop = PopCount.of(nb, nw);
                final EntryTable t = get(pop.index());
                System.out.format("%5d", t.size());
            }

            System.out.println();
        }
    }

    public static void main(String... args) throws InterruptedException, ExecutionException {
        open().dump();
    }

}
