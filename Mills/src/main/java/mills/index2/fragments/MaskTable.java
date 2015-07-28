package mills.index2.fragments;

import mills.ring.EntryTable;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  27.07.2015 17:04
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class MaskTable extends PartitionTable<RadialTable> {

    MaskTable(EntryTable root) {
        super(root);
    }

    @Override
    public int size() {
        return 128;
    }

    public static final MaskTable EMPTY = new MaskTable(EntryTable.EMPTY) {

        @Override
        public RadialTable get(int index) {
            return RadialTable.EMPTY;
        }
    };
}
