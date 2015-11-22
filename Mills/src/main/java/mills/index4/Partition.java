package mills.index4;

import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:34 PM
 */

/**
 * Class Partition represents a group of RingEntries matching a given PopCount and a given permutation mask.
 */
class Partition extends AbstractRandomList<EntryTable> {

    // table of entries matching PopCount and permutation mask.
    public final EntryTable root;

    Partition(EntryTable root) {
        this.root = root;
    }

    public int size() {
        return 0;
    }

    public EntryTable get(int index) {
        return EntryTable.EMPTY;
    }

    // return first index with rdc.rad >= rad or size()
    public int tail(RingEntry rad) {
        return 0;
    }

    public RdClop rdc(int index) {
        throw new IndexOutOfBoundsException("Partition::rdc");
    }

    public short etx(int index) {
        return -1; // empty
    }

    public static final Partition EMPTY = new Partition(EntryTable.EMPTY);
}
