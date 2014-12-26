package mills.index2;

import com.google.common.collect.ImmutableList;
import mills.bits.PopCount;
import mills.index.IndexList;
import mills.index.Partitions;
import mills.index.R2Index;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/26/14
 * Time: 6:30 PM
 */
public class R2Tables extends AbstractRandomList<R2Table> {

    public static int SIZE = PopCount.CLOSED.size();

    private final List<R2Table> tables;

    public final PopCount pop;

    private R2Tables(PopCount pop, List<R2Table> tables) {
        this.tables = ImmutableList.copyOf(tables);
        this.pop = pop;
        assert tables.size() == SIZE;
    }

    public PopCount pop() {
        return pop;
    }

    @Override
    public int size() {
        return SIZE;
    }

    @Override
    public R2Table get(int index) {
        return tables.get(index);
    }

    public R2Table get(PopCount clop) {
        return tables.get(clop.index);
    }

    public static class Builder extends RecursiveTask<R2Tables> {

        final EntryTables tables = new EntryTables();

        final Partitions partitions = Partitions.open();

        final PopCount pop;

        final ConcurrentLinkedQueue<R0Table.Builders> builders = new ConcurrentLinkedQueue<>();

        R0Table.Builders builders() {
            R0Table.Builders b = builders.poll();
            if(b==null)
                b = new R0Table.Builders(tables, partitions);
            return b;
        }

        void release(R0Table.Builders b) {
            b.reset();
            builders.add(b);
        }

        public Builder(PopCount pop) {
            this.pop = pop;
        }

        class Task extends RecursiveTask<List<R0Table>> {

            final RingEntry e2;

            final Task next;

            Task(RingEntry e2, Task next) {
                this.e2 = e2;
                this.next = next;
            }

            @Override
            protected List<R0Table> compute() {
                if(next!=null)
                    next.fork();

                R0Table.Builders builder = builders();
                builder.process(pop, e2);

                List<R0Table> result = ImmutableList.copyOf(builder);

                release(builder);

                return result;
            }
        }

        @Override
        protected R2Tables compute() {

            final R2Table.Builders builders = new R2Table.Builders();

            LinkedList<Task> tasks = new LinkedList<>();
            //Task next = null;

            for(int i=0; i<RingEntry.MAX_INDEX; ++i) {
                RingEntry e2 = RingEntry.of(RingEntry.MAX_INDEX-i-1);
                Task task = new Task(e2, null);
                //next = task;
                tasks.addFirst(task);
                task.fork();
            }
            //next.fork();

            while(!tasks.isEmpty()) {
                Task task = tasks.removeFirst();
                builders.process(task.e2, task.join());
            }

            return new R2Tables(pop, builders);
        }
    }

    public static void main(String ... args) {

        IndexList il = IndexList.create();

        final PopCount pop = PopCount.of(8,8);

        ForkJoinTask<R2Tables> task = new R2Tables.Builder(pop).fork();

        R2Index ix = il.get(pop);

        System.out.format("l%d%d%12d %4d\n", pop.nb, pop.nw, ix.range(), ix.values().size());

        R2Tables index = task.join();

        int count = 0;
        for (R2Table t : index) {
            count += t.range();
        }

        System.out.format("%s %11d\n", pop, count);

        for (PopCount clop : PopCount.CLOSED) {
            final R2Table t = index.get(clop.index);
            if(!t.isEmpty())
                System.out.format("  %s %9d %4d\n", clop, t.range(), t.size());
        }

/*        System.out.println();

        int n = 0;

        R2Table r2 = index.get(0);
        for (int i2 = 0; i2 < r2.size(); i2++) {
            RingEntry e2 = r2.entry(i2);
            R0Table r0t = r2.get(i2);
            R2Entry r2e = ix.values().get(i2);

            int n0 = r0t.size();

            for(int i0 = 0; i0<n0; ++i0) {
                RingEntry e0 = r0t.entry(i0);
                EntryTable t1x = r0t.get(i0);
                EntryTable t1y = r2e.values().get(i0);
                n += t1x.size();
                if(!t1x.equals(t1y)) {
                    t1x = r0t.get(i0);
                    System.out.format("%4d %4d %2s %2s\n", e2.index(), e0.index(), t1x.size(), t1y.size());
                }
            }

            if(r0t.size() != r2e.size())
                System.out.format("%d %4d %2s %2s\n", i2, e2.index(), r0t.size(), r2e.size());
        }

      System.out.format("count: %d\n", n);
*/
    }
}
