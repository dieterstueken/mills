package mills.ring;

import mills.bits.*;

import java.util.*;
import java.util.function.Consumer;

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
public class RingEntry extends BW {

    // number of possible entries
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
    private final short[] perm = new short[8];

    private EntryTable sisters;

    public EntryTable sisters() {

        if(sisters==null) {
            sisters = minimized().sisters;
        }

        return sisters;
    }

    // permutation group fingerprint
    public final PGroup grp;

    public final PGroup grp() {
        return grp;
    }

    public Player player(Sector sector) {
        int p = index/sector.pow3();
        return Player.of(p%3);
    }

    public final SingleEntry singleton = new SingleEntry(this);

    public SingleEntry singleton() {
        return singleton;
    }

    public final RingEntry swapped() {
        return Entries.TABLE.get(swapped);
    }

    // return entry with only radials set.
    public final RingEntry radials() {
        int radix = index%81;
        return index==radix ? this : Entries.RADIALS.get(radix());
    }

    // get radial index
    public final int radix() {
        return index%81;
    }

    // get permutation #i
    public final short perm(int i) {
        return perm[i& Perm.MSK];
    }

    public final RingEntry permute(Perm p) {
        return Entries.of(perm(p.ordinal()));
    }

    // return stable permutation mask
    public int pmeq() {
        return 0xff & meq;  // convert to positive int [0, 256[
    }

    // return stable permutation mask
    public int pmlt() {
        return 0xff & mlt;  // convert to positive int [0, 256[
    }

    public boolean anyMlt(int msk) {
        return (mlt&msk)==0;
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

    // return minimized value
    public short min() {
        return perm(mix);
    }

    public RingEntry minimized() {
        return mix==0 ? this : Entries.of(min());
    }

    /**
     * @return Occupied sectors.
     */
    public Set<Sector> sectors() {
        return b.or(w);
    }

    public Set<Sector> sectors(Player player) {

        switch(player) {
            case Black: return b;
            case White: return w;
            case None: return b.or(w).not();
        }

        return Collections.emptySet();
    }

    /**
     * Iterate all positions with fewer stones.
     * @param action to be taken on each minor position.
     */
    public void forEachMinor(Consumer<? super RingEntry> action) {
        sectors().forEach(sector->{
            RingEntry minor = withPlayer(sector, Player.None);
            action.accept(minor);
            //minor.forEachMinor(action);
        });
    }

    /**
     * setup a new player for a given sector.
     * @param sector to setup
     * @param player to set.
     * @return a RingEntry with given player at given sector.
     */
    public RingEntry withPlayer(Sector sector, Player player) {
        final Player current = player(sector);

        // noop
        if(current==player)
            return this;

        int index = this.index;
        final int pow3 = sector.pow3();
        index -= pow3 * current.ordinal();
        index += pow3 * player.ordinal();

        return Entries.of(index);
    }

    public RingEntry and(Patterns other) {
        return Entries.of(b.and(other.b), w.and(other.w));
    }

    public boolean contains(RingEntry other) {
        return b.contains(other.b) && w.contains(other.w);
    }

    private RingEntry(short index) {
        super(index);

        this.swapped = BW.index(w, b);

        perm[0] = index;

        byte mix = 0;
        byte meq = 1;
        byte min = 1;
        byte mlt = 0;

        for(int i=1; i<8; i++) {
            // generate permutations
            final short k = index(b.perm(i), w.perm(i));
            perm[i] = k;

            int m = 1<<i;

            // find if new permutation is smaller than current mix index
            if(k<perm[mix]) {
                mix = (byte)i;        // found better minimal index
                min = (byte)m;        // reset any previous masks
            }
            else
            if(k==perm[mix])
                min |= m;   // add additional minimum to mask

            if(k<index)         // found a new mlt index
                mlt |= m;

            if(k==index)         // found a new meq index
                meq |= m;
        }

        assert perm[0] == index : "RingEntry: index mismatch";

        this.meq = meq;
        this.mlt = mlt;
        this.min = min;
        this.mix = mix;

        this.grp = PGroup.group(pmeq());

        // precalculate sisters
        if(mix==0) {
            short[] s = Arrays.copyOf(perm, 8);
            Arrays.sort(s);

            int n=0;
            for(int i=1; i<8; ++i) {
                short k = s[i];
                if(k>s[n])
                    s[++n] = k;
            }

            sisters = n==0 ? singleton : EntryTable.of(s, n+1);
        }
    }

    static RingEntry[] table() {
        RingEntry[] table = new RingEntry[MAX_INDEX];
        Arrays.parallelSetAll(table, index -> new RingEntry((short) index));
        return table;
    }

    static List<EntryTable> sisters(EntryTable minimized) {

        EntryTable[] result = new EntryTable[MAX_INDEX];

        Set<RingEntry> tmp = new TreeSet<>();

        for (RingEntry e : minimized) {
            for (Perm perm : Perm.VALUES) {
                tmp.add(e.permute(perm));
            }

            EntryTable sisters = EntryTable.of(tmp);
            sisters.forEach(s -> result[s.index] = sisters);
            tmp.clear();
        }

        return List.of(result);
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

            candidates >>>= 1;

            if(candidates%16==0) {
                candidates >>>= 4;
                pi += 4;
            }

            if(candidates%4==0) {
                candidates >>>= 2;
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
        sb.append(String.format(" ix%d m:%02x e:%02x l:%02x ", pmix(), pmin(), pmeq(), pmlt()));

        // add permutation group (reversed)
        for(int i=0; i<8; i++)
            sb.append(String.format("%5d", perm[7-i]));

        return sb;
    }

    public int hashCode() {
        return index;
    }

    public static final Comparator<RingEntry> COMPARATOR = (o1, o2) -> {
        if(o1==o2)
            return 0;

        if(o1==null)
            return -1;

        return Short.compare(o1.index, o2.index);
    };

    ////////////////////////////////////////////////////////////////////////

    public static void main(String ... args) {

        int stat1[] = new int[9];
        int stat2[] = new int[9];

        for(RingEntry e: Entries.TABLE) {
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

}