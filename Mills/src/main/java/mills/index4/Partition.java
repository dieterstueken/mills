package mills.index4;

import mills.ring.EntryTable;

import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11/15/15
 * Time: 6:34 PM
 */

/**
 * Class Partition represents a group of RingEntries matching a given PopCount and a given permutation mask.
 */
abstract class Partition {

    // table of entries matching PopCount and permutation mask.
    public final EntryTable root;

    Partition(EntryTable root) {
        this.root = root;
    }

    abstract public EntryTable get(RdClop index);

    abstract public List<EntryTable> tables();

    public static final Partition EMPTY = new Partition(EntryTable.EMPTY) {

        @Override
        public EntryTable get(RdClop index) {
            return EntryTable.EMPTY;
        }

        @Override
        public List<EntryTable> tables() {
            return Collections.emptyList();
        }
    };
}
