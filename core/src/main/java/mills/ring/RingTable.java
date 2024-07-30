package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.09.2010
 * Time: 13:37:50
 */

import mills.bits.Patterns;
import mills.bits.Perm;

import java.util.Arrays;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Class RingTable is the complete EntryTable of 6561 RingEntries.
 */
class RingTable extends RingArray implements IndexedEntryTable {

    RingTable() {
        super(new RingEntry[MAX_INDEX]);
        Arrays.parallelSetAll(entries, this::newEntry);
    }

    @Override
    public int size() {
        return MAX_INDEX;
    }

    @Override
    public int getIndex() {
        return MAX_INDEX;
    }

    // for the full table there is no need to search any entry.
    public int findIndex(int index) {
        return inRange(index) ? index : -1;
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

        return (short) index;
    }

    private RingEntry newEntry(int index) {
        return newEntry((short) index);
    }

    private RingEntry newEntry(final short index) {

        byte mix = 0;
        byte meq = 1;
        byte min = 1;
        byte mlt = 0;

        Patterns bw = new Patterns(index);

        short pm = index;
        short[] perm = new short[8];
        perm[0] = index;

        for (byte i = 1, m = 2; i < 8; i++, m<<=1) {
            // generate permutations
            short pi = perm[i] = bw.perm(i);

            // find if new permutation is smaller than current mix index
            if (pi < pm) {
                mix = i;        // found better minimal index
                min = m;        // reset any previous masks
                pm = pi;
            } else if (pi == pm)
                min |= m;       // add additional minimum to mask

            if (pi < index)     // found a new mlt index
                mlt |= m;

            if (pi == index)    // found a new meq index
                meq |= m;
        }

        if (mix == 0) {
            return new RingEntry(index, meq, mlt, min, (byte)0, perm) {

                @Override
                public RingEntry entry(int index) {
                    return entries[index];
                }

                @Override
                public boolean isMin() {
                    return true;
                }

                @Override
                public short min() {
                    return index;
                }

                @Override
                public RingEntry minimized() {
                    return this;
                }

                @Override
                public Perm pmix() {
                    return Perm.R0;
                }
            };
        } else {
            return new RingEntry(index, meq, mlt, min, mix, perm) {

                @Override
                public RingEntry entry(int index) {
                    return entries[index];
                }

                @Override
                public boolean isMin() {
                    return false;
                }
            };
        }
    }
}
