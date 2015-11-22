package mills.index4;

import mills.bits.PopCount;
import mills.ring.EntryTables;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:52 PM
 */

/**
 * Class Partition is a lookup table for groups of
 * EntryTables of constant pop count matching a given msk
 */
public class Partitions {

    final EntryTables registry;

    final List<PartitionTable> tables;

    Partitions(EntryTables registry, List<PartitionTable> tables) {
        this.registry = registry;
        this.tables = tables;
        assert tables.size() == PopCount.SIZE;
    }

    public PartitionTable get(PopCount pop) {
        int index = pop == null ? -1 : pop.index;
        return index < 0 ? PartitionTable.EMPTY : tables.get(index);
    }
}
