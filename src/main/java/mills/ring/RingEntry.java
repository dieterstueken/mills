package mills.ring;

import mills.bits.*;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.2010
 * Time: 14:45:47
 */

/**
 * class RingEntry represents a configuration of 8 W&W stones on 8 positions on a ringTable.
 * There are 3^8 = 6561 possible configurations.
 * Each configuration may be permuted by 8 symmetry operations.
 * All possible tables are kept in a static lookup table.
 * The index
 */
public class RingEntry extends BW implements Comparable<RingEntry> {

    // number of possible tables
    public static final int MAX_INDEX = 81*81;


    // The byte values/masks may save some space, but become negative if expanding to int.
    // Using access functions will clip the sign bit.

    public final short swapped;

    // bit mask of stable permutations (==min for minimized tables)
    public final byte meq;

    // bit mask of permutations which reduces this entry (==min for minimized tables)
    public final byte mlt;

    // bit mask of minimized permutations
    public final byte min;

    // index of first permutation index which minimizes this entry
    public final byte mix;

    // permutation group kept as a short index.
    private final short perm[] = new short[8];

    public final List<RingEntry> permutations = AbstractRandomList.virtual(8, index -> TABLE.get(perm[index]));

    // permutation group fingerprint
    public final PGroup grp;

    public Player player(Sector sector) {
        int p = index/sector.pow3();
        return Player.of(p%3);
    }

    public List<Player> players = new AbstractRandomArray<Player>(8) {

        @Override
        public Player get(int i) {
            return player(Sector.of(i));
        }
    };

    public final SingleEntry singleton = new SingleEntry(this);

    public SingleEntry singleton() {
        return singleton;
    }

    // index of this entry
    public final Short index() {
        return index;
    }

    public final RingEntry swapped() {
        return TABLE.get(swapped);
    }

    public final RingEntry radials() {
        int radials = index/81;
        return TABLE.get(81*radials);
    }

    // get radial index
    public final int radix() {
        return index/81;
    }

    // get permutation
    public final short perm(int i) {
        return perm[i& Perm.MSK];
    }

    // return stable permutation mask
    public int pmeq() {
        return 0xff & meq;  // convert to positive int [0, 256[
    }

    // return stable permutation mask
    public int pmlt() {
        return 0xff & mlt;  // convert to positive int [0, 256[
    }

    // return minimum permutation mask
    public int pmin() {
        return 0xff & min;  // convert to positive int [0, 256[
    }

    // remains stable under given permutation mask
    public boolean stable(int msk) {
        return (pmin() & msk) == 0;
    }

    // return first permutation index which minimizes this entry
    public byte pmix() {
        return mix;
    }

    // return if this entry is normalized
    public boolean isMin() {
        return mix==0;
    }

    public RingEntry player(final Sector sector, final Player player) {
        final Player current = player(sector);

        // noop
        if(current==player)
            return this;

        int index = this.index;
        final int pow3 = sector.pow3();
        index -= pow3 * current.ordinal();
        index += pow3 * player.ordinal();

        return of(index);
    }

    public RingEntry and(RingEntry other) {
        return of(b.and(other.b), w.and(other.w));
    }

    private RingEntry(short index) {
        super(index);

        this.swapped = BW.index(w, b);

        perm[0] = index;

        byte mix = 0;
        byte meq = 1;
        byte min = 1;
        byte mlt = 0;

        for(byte i=0; i<8; i++) {
            // generate permutations
            final short k = index(b.perm(i), w.perm(i));
            perm[i] = k;

            int m = 1<<i;

            // find if new permutation is smaller than current mix index
            if(k<perm[mix]) {
                mix = i;        // found better minimal index
                min = 0;        // reset any previous masks
            }
            else
            if(k==perm[mix])
                min |= m;   // add additional minimum to mask

            if(k<index)         // found a new mlt index
                mlt |= m;

            if(k==index)         // found a new meq index
                meq |= m;
        }

        this.meq = meq;
        this.mlt = mlt;
        this.min = min;
        this.mix = mix;

        this.grp = PGroup.group(pmeq());
    }

    // return short instead of byte to avoid any negative values

    /**
     * Return mask of reduction bits.
     * Since bit 0 is always zero, return 7 bits only: [0, 127]
     * @param r0 entry to obey
     * @return mask of permutations which will reduce this i20 combination.
     */

    public short mlt20s(final RingEntry r0) {
        return (short) (mlt20(r0)/2);
    }

    // utility function to speed up comparison by mapping to an integer.
    static int i20(int i2, int i0) {
        return i2 | (i0<<16);
    }

    // return a permutation mask of all reducing operations.
    // Bit#0 is set to 1 if an initial swap reduces the rank.
    public int mlt20(final RingEntry r0) {

        // r0 must be <= r2, else swap and tag
        if(index<r0.index)
            return r0.mlt20(this) | 1;

        // any reduction needs at least one lesser index
        int candidates = (mlt|r0.mlt)&0xff;
        if(candidates==0)
            return 0;

        short i2 = index;
        short i0 = r0.index;

        // current index value
        final int i20 = i20(i2, i0);

        // identity transformation won't reduce ever.
        int result = 0;
        int pi = 1;

        while(candidates!=0) {

            candidates >>= 1;

            if(candidates%16==0) {
                candidates >>= 4;
                pi += 4;
            }

            if(candidates%4==0) {
                candidates >>= 2;
                pi += 2;
            }

            if(candidates%2!=0) {
                i2 = perm[pi];
                i0 = r0.perm[pi];

                // check swapped combinations too (swaps are not tagged here)
                if(i20(i2, i0)<i20 || i20(i0, i2)<i20)
                    result |= (1<<pi);
            }

            ++pi;
        }

        return result;
    }

    public String toString() {
        return toString(new StringBuilder()).toString();
    }

    public StringBuilder toString(final StringBuilder sb) {

        // prepend pop
        sb.append(String.format("%1d%1d[%1d%1d] ", pop.nb(), pop.nw(), b.mcount, w.mcount));
        pattern(sb);

        // add permutation info
        sb.append(String.format(" %d:%02x %02x %02x ", pmix(), pmin(), pmeq(), pmlt()));

        // add permutation group (reversed)
        for(int i=0; i<8; i++)
            sb.append(String.format("%5d", perm[7-i]));

        return sb;
    }

    public int hashCode() {
        return index;
    }

    //////////////////// static utilities functions on RinEntry index ////////////////////

    public static Predicate<RingEntry> IS_MIN = RingEntry::isMin;

    /**
     * A virtual list of of RinEntries to be materialized be a copy.
     * @return a virtual list of of RinEntries.
     */
    static RingEntry[] entries() {
        RingEntry[] entries = new RingEntry[MAX_INDEX];
        Arrays.setAll(entries, index -> new RingEntry((short) index));
        return entries;
    }

    /**
     * An immutable list of all tables.
     */
    public static final RingTable TABLE = new RingTable();

    public static final EntryTable MINIMIZED = TABLE.filter(IS_MIN);

    // get radial stones
    public static RingEntry radix(int index) {
        return TABLE.get(81*index);
    }

    public static RingEntry of(int index) {
        return TABLE.get(index);
    }

    public static RingEntry of(Pattern b, Pattern w) {
        return TABLE.get(BW.index(b,w));
    }

    ////////////////////////////////////////////////////////////////////////

    public static void main(String ... args) {

        int stat1[] = new int[9];
        int stat2[] = new int[9];

        for(RingEntry e:TABLE) {
            System.out.println(e.toString());
            ++stat1[e.grp.ordinal()];

            if(e.isMin())
                ++stat2[e.grp.ordinal()];
        }

        for(PGroup p:PGroup.values()) {

            final int count1 = stat1[p.ordinal()];
            final int count2 = stat2[p.ordinal()];
            final int meq = p.meq();

            System.out.format("%02x %d %4d %4d\n", meq, Integer.bitCount(meq), count1, count2);
        }
    }

    public static final Comparator<RingEntry> COMPARATOR = (o1, o2) -> {
        if(o1==o2)
            return 0;

        if(o1==null)
            return -1;

        return Short.compare(o1.index, o2.index);
    };

    @Override
    public int compareTo(RingEntry o) {
        return Short.compare(index, o.index);
    }
}