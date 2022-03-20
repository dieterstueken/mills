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
class RingTable extends AbstractEntryTable implements IndexedEntryTable {

    private final RingEntry[] entries = new RingEntry[MAX_INDEX];

    private final int hashCode;

    RingTable() {
        Arrays.setAll(entries, i -> entry((short) i));
        this.hashCode = Arrays.hashCode(entries);
    }

    @Override
    public int size() {
        return MAX_INDEX;
    }

    @Override
    public int getIndex() {
        return MAX_INDEX;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    boolean inRange(int index) {
        return index >= 0 && index < MAX_INDEX;
    }

    // for the full table there is no need to search any entry.
    public int findIndex(int index) {
        return inRange(index) ? index : -1;
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

        return (short) index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        return (o instanceof RingTable that) ?
             Arrays.equals(entries, that.entries) : super.equals(o);
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    private RingEntry entry(short index) {

        short[] perm = new short[8];
        perm[0] = index;
        short pm = index;

        byte mix = 0;
        byte meq = 1;
        byte min = 1;
        byte mlt = 0;

        Patterns bw = new Patterns(index);

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

        if (mix == 0)
            return new MEntry(index, meq, mlt, min, perm, sisters(perm));

        short mindex = pm;
         
        // get already minimized sisters
        EntryTable sisters = entries[pm].sisters;

        return new Entry(index, meq, mlt, min, mix, perm, sisters) {

            @Override
            public boolean isMin() {
                return false;
            }

            @Override
            public short min() {
                return mindex;
            }
        };
    }

    class Entry extends RingEntry {

        Entry(short index, byte meq, byte mlt, byte min, byte mix, short[] perm, EntryTable sisters) {
            super(index, meq, mlt, min, mix, perm, sisters);
        }

        @Override
        RingEntry entryOf(int index) {
            return entries[index];
        }
    }

    class MEntry extends Entry {

        MEntry(short index, byte meq, byte mlt, byte min, short[] perm, EntryTable sisters) {
            super(index, meq, mlt, min, (byte)0, perm, sisters);
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
    }

    private EntryTable sisters(short[] perm) {
        short[] s = Arrays.copyOf(perm, 8);
        Arrays.sort(s);

        int n = 0;
        for (int i = 1; i < 8; ++i) {
            short k = s[i];
            if (k > s[n])
                s[++n] = k;
        }

        s = Arrays.copyOfRange(s, 0, n + 1);
        return EntryArray.of(s);
    }
}
