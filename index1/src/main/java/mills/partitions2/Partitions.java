package mills.partitions2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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

    public final LePopTables lePops;

    public final List<Partition> partitions;  // pop

    public final List<EntryTable> tables;

    public final IndexTable ranges;

    Partitions(LePopTables lePops, List<Partition> partitions) {
        this.lePops = lePops;
        this.partitions = List.copyOf(partitions);
        ranges = IndexTable.sum(partitions, p -> p.tables.size());

        List<EntryTable> tables = new ArrayList<>(ranges.range());

        for (Partition p : partitions) {
            tables.addAll(p.tables);
        }

        this.tables = List.copyOf(tables);
    }

    public EntryTable getPartition(PopCount pop, int msk) {
        return partitions.get(pop.index).groups.get(msk).root;
    }

    public EntryTable lePop(PopCount pop, PopCount clop, int count) {
        return count>0 ? lePops.get(pop, clop, count) :  lePops.get(pop, clop);
    }

    // pop or clop may be null after subtraction
    public int getKey(PopCount pop, int msk, PopCount clop, int radials) {
        return pop==null||clop==null ? 0 : getKey(pop.index, msk, clop, radials);
    }

    public int getKey(int pop, int msk, PopCount clop, int radials) {
        int key = partitions.get(pop).getKey(msk, clop, radials);

        if(key>0 && pop>0)
            key += ranges.get(pop-1);

        assert key <= tables.size();
        return key;
    }

    public EntryTable getTable(int key) {
        if(key<0)
            return RingEntry.of(-1-key).singleton;

        return key==0 ? EntryTable.EMPTY : tables.get(key-1);
    }

    public List<EntryTable> r1Table(final short[] keys) {
        return new AbstractRandomList<EntryTable>() {

            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public EntryTable get(int index) {
                return getTable(keys[index]);
            }
        };
    }

    public List<EntryTable> entryTables(final short[] keys, int size) {

        if(keys==null || size==0)
            return Collections.emptyList();

        if(size>1)
            return r1Table(Arrays.copyOf(keys, size));

        int key = keys[0];
        EntryTable table = getTable(key);

        return Collections.singletonList(table);
    }

    public static Partitions build() {

        ForkJoinTask<LePopTables> lePopTask = LePopTables.task().fork();

        Partition partitions[] = new Partition[100];
        Arrays.fill(partitions, Partition.EMPTY);

        List<RecursiveAction> tasks = new ArrayList<>(100);

        PopCount.TABLE.stream().filter(pop -> pop.sum() <= 8).forEach(pop -> {
            RecursiveAction task = new RecursiveAction() {

                @Override
                protected void compute() {
                    partitions[pop.index] = Partition.build(pop);
                }

                @Override
                public String toString() {
                    return String.format("build %s", pop);
                }
            };

            tasks.add(task);
        });

        ForkJoinTask.invokeAll(tasks);

        return new Partitions(lePopTask.join(), Arrays.asList(partitions));
    }

    public static void main(String ... args) {
        Partitions pt = build();

        for (PopCount pop : PopCount.TABLE) {
            Partition p = pt.partitions.get(pop.index);

            if(p.isEmpty())
                continue;

            System.out.format("%s %3dr %3dt %3ds %4dc: ", pop, p.groups.get(0).root.size(), p.tables.size(), p.set.size(), p.count());

            for (PartitionGroup pg : p.set) {
                System.out.format(" %d:%d", pg.root.size(), pg.count());
            }

            System.out.println();
        }

        System.out.println(pt.tables.size());
    }
}
