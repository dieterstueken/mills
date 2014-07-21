package mills.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/21/14
 * Time: 9:14 AM
 */
public class Partitions {

    final List<Partition> partitions;  // pop

    List<EntryTable> tables;

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
}
