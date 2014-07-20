package mills.index2;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/29/14
 * Time: 2:42 PM
 */

public class PartitionTables extends AbstractRandomList<List<EntryTable>> {

    final List<Supplier<List<EntryTable>>> partitions = new ArrayList<>(PopCount.SIZE);

    PartitionTables() {

        for(int i=0; i<PopCount.SIZE; ++i) {
            ForkJoinTask<List<EntryTable>> task = task(i);

            // first level Supplier will call join()
            partitions.add(task::join);

            // submit after the entry was set up.
            task.fork();
        }
    }

    @Override
    public int size() {
        return PopCount.SIZE;
    }

    @Override
    public List<EntryTable> get(int index) {
        return partitions.get(index).get();
    }

    private ForkJoinTask<List<EntryTable>> task(int index) {

        return new RecursiveTask<List<EntryTable>>() {

            @Override
            protected List<EntryTable> compute() {
                List<EntryTable> partition = partition(index);

                // replace supplier by final result to speed up further access.
                partitions.set(index, () -> partition);

                return partition;
            }
        };
    }

    private static final List<EntryTable> EMPTY_TABLE = AbstractRandomArray.of(128, EntryTable.EMPTY);

    /**
     * Build a new partition table for a given index.
     * @param index if the partition to generate.
     * @return a new list of partitions.
     */
    private static List<EntryTable> partition(int index) {

        // filter off a root table by pop count.
        PopCount pop = PopCount.get(index);
        EntryTable root = RingEntry.MINIMIZED.filter(pop.eq);

        if (root.isEmpty())
            return EMPTY_TABLE;

        /**
         * The table[128] contains only a few different entries.
         * PGroup.Set maps some index to an unique index with an equivalent entry which may be copied.
         */

        final Set<PGroup> groups = PGroup.groups(root);

        // table to populate
        EntryTable[] table = new EntryTable[128];

        // populate all partitions
        for (int msk = 0; msk < 128; ++msk) {

            EntryTable et = table[msk];
            if (et != null)
                continue;   // done

            // try get an entry which may have been calculated before.
            int part = PGroup.pindex(groups, msk);

            et = table[part];
            if (et == null) {
                // generate a new partition
                et = root.filter(PGroup.filter(msk));
                table[part] = et;
            }

            // populate entry
            if (part != msk)
                table[msk] = et;
        }

        return AbstractRandomList.of(table);
    }

    public static final PartitionTables INSTANCE = new PartitionTables();

    public static List<EntryTable> of(int pop) {
        return INSTANCE.get(pop);
    }


    public static void main(String ... args) {

        Set<EntryTable> tables = new TreeSet<>();

        List<PopCount> mops = new ArrayList<>(25);
        for(int mb=0;mb<5;mb++)
            for(int mw=0;mw<5;mw++)
                mops.add(PopCount.of(mb,mw));

        INSTANCE.forEach(list -> {
                    list.forEach(t -> {
                                tables.add(t);

                            });
                }
        );

        System.out.format("%d\n", tables.size());
    }
}
