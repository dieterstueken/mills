package mills.index2.fragments;

import mills.ring.EntryTable;
import mills.ring.RingEntry;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 08:36
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class RadialTable extends PartitionTable<ClopTable> {

    RadialTable(EntryTable root) {
        super(root);
    }

    @Override
    public int size() {
        return 81;
    }

    public ClopTable get(RingEntry radial) {
        return get(radial.radix());
    }

    static final RadialTable EMPTY = new RadialTable(EntryTable.EMPTY) {

        @Override
        public ClopTable get(int index) {
            return ClopTable.EMPTY;
        }
    };
}
