package mills.index4;

import mills.ring.EntryTable;
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
        return 1;
    }

    public EntryTable get(int index) {
        return index==0 ? root : EntryTable.EMPTY;
    }

    public int index(RdClop rdc) {
        return rdc==null ? 0 : -1;
    }

    public static final Partition EMPTY = new Partition(EntryTable.EMPTY);
}
