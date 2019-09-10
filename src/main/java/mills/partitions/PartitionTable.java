package mills.partitions;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.04.16
 * Time: 10:15
 */

import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.util.AbstractRandomArray;

import java.util.Collections;
import java.util.List;

/**
 * A PartitionTable provides a list of 128 RingTables for a given restriction mask [0,128[
 * Each partition contains minimized ring entries only.
 *
 */
abstract public class PartitionTable<P> extends AbstractRandomArray<P> {

    public final EntryTable lePop;

    public final List<P> partitions;

    public PartitionTable(List<P> partitions, EntryTable lePop) {
        super(128);
        this.lePop = lePop;
        this.partitions = partitions;
    }

    public static <P> PartitionTable<P> empty(P empty) {
        return new PartitionTable<P>(Collections.emptyList(), Entry.TABLE) {

            @Override
            public P get(int index) {
                return empty;
            }
        };
    }

    public static <P> PartitionTable<P> of(List<P> partitions, List<P> table, EntryTable lePop) {
        partitions = List.copyOf(partitions);
        return new PartitionTable<P>(partitions, lePop) {

            @Override
            public P get(int index) {
                return table.get(index);
            }
        };
    }
}
