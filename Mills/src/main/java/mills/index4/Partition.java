package mills.index4;

import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.IntConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:34 PM
 */

/**
 * Class Partition represents a group of RingEntries matching a given PopCount and a given permutation mask.
 */
class Partition extends AbstractMap<RdClop, EntryTable> {

    // table of entries matching PopCount and permutation mask.
    public final EntryTable root;

    Partition(EntryTable root) {
        this.root = root;
    }

    @Override
    public Set<Entry<RdClop, EntryTable>> entrySet() {
        return Collections.emptySet();
    }

    @Override
    public Set<RdClop> keySet() {
        return super.keySet();
    }

    @Override
    public Collection<EntryTable> values() {
        return super.values();
    }

    public void process(RingEntry rad, IntConsumer consumer) {}

    public static final Partition EMPTY = new Partition(EntryTable.EMPTY);
}
