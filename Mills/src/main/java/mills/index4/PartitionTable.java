package mills.index4;

import mills.ring.EntryTable;
import mills.util.AbstractRandomArray;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 7:07 PM
 */
public class PartitionTable {

    public static final PartitionTable EMPTY = new PartitionTable(EntryTable.EMPTY, EntryTable.EMPTY,
            AbstractRandomArray.of(128, Partition.EMPTY), Collections.emptyList());

    public final EntryTable root;

    public final EntryTable lePop;

    final List<Partition> partitions;

    // subset of different partitions (debug only)
    final List<Partition> pset;

    public PartitionTable(EntryTable root, EntryTable lePop, List<Partition> partitions, List<Partition> pset) {
        this.root = root;
        this.lePop = lePop;
        this.partitions = partitions;
        this.pset = pset;
    }

    public Partition get(int index) {
        return partitions.get(index);
    }

    public String toString() {
        return String.format("%2d", pset.size());
    }
}
