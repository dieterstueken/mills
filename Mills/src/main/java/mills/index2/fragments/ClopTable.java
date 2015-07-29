package mills.index2.fragments;

import mills.bits.PopCount;
import mills.ring.EntryTable;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:00
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class ClopTable extends PartitionTable<EntryTable> {

    ClopTable(EntryTable root) {
        super(root);
    }

    public EntryTable get(PopCount clop) {
        return get(clop.index);
    }

    @Override
    public int size() {
        return 25;
    }

    public static final ClopTable EMPTY = new ClopTable(EntryTable.EMPTY) {
        @Override
        public EntryTable get(int index) {
            return EntryTable.EMPTY;
        }

        @Override
        public String toString() {
            return "empty";
        }
    };
}
