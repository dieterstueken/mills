package mills.index1;

import mills.bits.PopCount;
import mills.index1.partitions.LePopTable;
import mills.index1.partitions.PartitionTables;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.concurrent.ForkJoinTask;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  13.12.12 12:48
 * modified by: $Author$
 * modified on: $Date$
 */

/**
 * Build a partition for each call to get(index)
 */
public class Partitions extends AbstractRandomList<R2Index> {

    public final PartitionTables<EntryTable> partitions;
    public final LePopTable lePopTable;

    public Partitions(PartitionTables<EntryTable> partitions, LePopTable lePopTable) {
        this.lePopTable = lePopTable;
        this.partitions = partitions;
    }

    public static PartitionTables<EntryTable> buildPartitions() {
        return PartitionTables.build();
    }

    public static Partitions build() {
        // build parallel
        ForkJoinTask<PartitionTables<EntryTable>> pt = ForkJoinTask.adapt(Partitions::buildPartitions).fork();
        LePopTable lePopTable = LePopTable.build();
        PartitionTables<EntryTable> partitions = pt.join();
        return new Partitions(partitions, lePopTable);
    }

    @Override
    public int size() {
        return PopCount.SIZE;
    }

    @Override
    public R2Index get(int pc) {
        final PopCount pop = PopCount.TABLE.get(pc);
        R2Index index =  new T2Builder(pop, () -> new T0Builder(partitions, lePopTable)).build();
        return index;
    }

    public EntryTable lePop(PopCount pop) {
        return lePopTable.get(pop);
    }

    public EntryTable partition(PopCount pop, int mlt) {
        return partitions.get(pop).get(mlt);
    }
}
