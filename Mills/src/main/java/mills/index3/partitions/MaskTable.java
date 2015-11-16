package mills.index3.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  27.07.2015 17:04
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class MaskTable extends PartitionTable<RadialTable> {

    public final EntryTable root;

    MaskTable(EntryTable root) {
        this.root=root;
    }

    abstract public RadialTable get(int index);

    @Override
    public int size() {
        return 128;
    }

    abstract public PopCount pop();

    public static final MaskTable EMPTY = new MaskTable(EntryTable.EMPTY) {

        @Override
        public RadialTable get(int index) {
            return RadialTable.EMPTY;
        }

        @Override
        public PopCount pop() {
            return null;
        }

        @Override
        public String toString() {
            return "empty";
        }
    };
}
