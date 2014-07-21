package mills.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partitions {

    private final List<Partition> partitions;  // pop

    private final List<EntryTable> tables = new ArrayList<>();

    private final IndexTable ranges;

    public Partitions(List<Partition> partitions) {
        this.partitions = partitions;

        for (Partition p : partitions) {
            tables.addAll(p.tables);
        }

        ranges = IndexTable.sum(partitions, p->p.tables.size());
    }

    public int getKey(int pop, int msk, int clop, int radials) {
        int key = partitions.get(pop).getKey(msk, clop, radials);
        if(pop>0)
            key += ranges.get(pop);

        return key;
    }

    public EntryTable getTable(int key) {
        if(key==-1)
            return EntryTable.EMPTY;

        if(key<-1)
            return RingEntry.of(1-key).singleton;

        return tables.get(key);
    }

    public static Partitions build() {

        Partition partitions[] = new Partition[100];
        Arrays.fill(partitions, Partition.EMPTY);

        List<RecursiveAction> tasks = new ArrayList<>(100);

        for (PopCount pop : PopCount.TABLE) {
            if(pop.sum()<=8) {
                RecursiveAction task = new RecursiveAction() {
                    @Override
                    protected void compute() {
                        Partition partition = new Partition.Builder().partition(pop);
                        partitions[pop.index] = partition;
                    }
                };

                tasks.add(task);
            }
        }

        ForkJoinTask.invokeAll(tasks);

        return new Partitions(Arrays.asList(partitions));
    }

    public static Partitions get() {
        return SUPPLIER.get();
    }

    private static Supplier<Partitions> SUPPLIER = new Supplier<Partitions>() {

        final ForkJoinTask<Partitions> task = new RecursiveTask<Partitions>() {

            @Override
            protected Partitions compute() {
                Partitions partitions = build();
                SUPPLIER = () -> partitions;
                return partitions;
            }
        };

        final AtomicBoolean forked = new AtomicBoolean(false);

        @Override
        public Partitions get() {
            if(!forked.getAndSet(true))
                task.fork();

            return task.join();
        }
    };

    public static void main(String ... args) {
        Partitions pt = get();

        for (PopCount pop : PopCount.TABLE) {
            Partition p = pt.partitions.get(pop.index);

            if(p.isEmpty())
                continue;

            System.out.format("%s %3d %3d %4d: ", pop, p.set.size(), p.tables.size(), p.count());

            for (PartitionGroup pg : p.set) {
                System.out.format(" %d:%d", pg.root.size(), pg.groups.size());
            }

            System.out.println();
        }
    }
}
