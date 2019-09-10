package mills.ring;

import mills.bits.BW;
import mills.bits.Pattern;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  10.09.2019 12:02
 * modified by: $
 * modified on: $
 */
public interface Entry {

    /**
     * Immutable tables.
     */
    EntryTable TABLE = new RingTable();
    EntryTable RADIALS = TABLE.subList(0, 81);
    EntryTable MINIMIZED = TABLE.filter(RingEntry::isMin);
    RingEntry EMPTY = of(0);

    //////////////////// static utilities functions on RinEntry index ////////////////////

    static RingEntry of(int index) {
        return TABLE.get(index);
    }

    static RingEntry of(Pattern b, Pattern w) {
        return TABLE.get(BW.index(b,w));
    }
}
