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

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partitions {

    final List<Partition> partitions;  // pop

    List<EntryTable> tables = new ArrayList<>();

    IndexTable ranges;

    public Partitions(List<Partition> partitions) {
        this.partitions = partitions;

        for (Partition p : partitions) {
            tables.addAll(p.tables);
        }

        ranges = IndexTable.sum(partitions, p->p.tables.size());
    }

    public int getKey(PopCount pop, int msk, PopCount clop, int radials) {
        int key = partitions.get(pop.index).getKey(msk, clop, radials);
        key += baseKey(pop.index);
        return key;
    }

    private short baseKey(int index) {
        return 0;
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

    public static void main(String ... args) {
        Partitions p = build();
        System.out.println(p.tables.size());
    }
}
