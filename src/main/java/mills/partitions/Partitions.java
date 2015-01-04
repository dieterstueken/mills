package mills.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/27/14
 * Time: 1:34 PM
 */
public class Partitions {

    final List<EntryTable> lePopTable;

    final List<Partition> partitions;

    public Partitions(List<EntryTable> lePopTable, List<Partition> partitions) {
        this.lePopTable = lePopTable;
        this.partitions = partitions;
    }

    public EntryTable lePop(PopCount pop) {
        return lePopTable.get(pop.index);
    }

    public Partition partition(PopCount pop) {
        return partitions.get(pop.index);
    }
}
