package mills.ring;

import mills.bits.*;

import java.util.Comparator;

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
abstract public class RingEntry extends BW {

    // number of possible entries
    public static final int MAX_INDEX = 81*81;

    // The byte values/masks may save some space, but become negative if expanding to int.
    // Using access functions will clip the sign bit.

    public final short minimized;

    public final short inverted;

    // bit mask of stable permutations (==min for minimized tables)
    public final byte meq;

    // bit mask of permutations which reduces this entry (==0 for minimized tables)
    public final byte mlt;

    // bit mask of minimized permutations
    public final byte pmin;

    // index of first permutation index which minimizes this entry
    public final byte mix;

    // permutation group kept as a short index.
    private final short[] perm;

    abstract public RingEntry entry(int index);

    public final Player player(Sector sector) {
        int p = index/sector.pow3();
        return Player.of(p%3);
    }

    public boolean isEmpty() {
        return false;
    }

    private final SingletonTable singleton = SingletonTable.create(this);

    public SingletonTable singleton() {
        return singleton;
    }

    public final RingEntry inverted() {
        return entry(inverted);
    }

    // return entry with only radials set.
    public final RingEntry radials() {
        int radix = index%81;
        return index==radix ? this : entry(index%81);
    }

    // get radial index
    public final int radix() {
        return index%81;
    }

    /**
     * Calculate effective clop count with external radials.
     * @param rad external radials.
     * @return effective clop count
     */
    public final PopCount clop(RingEntry rad) {
        return and(rad).radials().pop.add(clop());
    }

    // reversed
    public final PopCount clops(RingEntry rad) {
        return and(rad).radials().pop.add(rad.clop());
    }

    // get permutation #i
    public final short perm(int i) {
        return perm[i&7];
    }

    public final RingEntry permute(int i) {
        return entry(perm[i&7]);
    }

    public final RingEntry permute(Perm p) {
        return permute(p.ordinal());
    }

    // return stable permutation mask
    public final int pmeq() {
        return 0xff & meq;  // convert to positive int [0, 256[
    }

    // return stable permutation mask
    public final int pmlt() {
        return 0xff & mlt;  // convert to positive int [0, 256[
    }

    // return if this is stable for all permutations given.
    public final boolean stable(Perms perms) {
        return (mlt & perms.getIndex())==0;
    }

    // return minimum permutation mask
    public final int pmin() {
        return 0xff & pmin;  // convert to positive int [0, 256[
    }

    // remains stable under given permutation mask
    public final boolean stable(int msk) {
        return (pmin() & msk) == 0;
    }

    // return first permutation index which minimizes this entry
    public Perm pmix() {
        return Perm.get(mix);
    }

    // return if this entry is normalized
    public boolean isMin() {
        return mix==0;
    }

    // return minimized value
    public short min() {
        return minimized; // perm(mix);
    }

    public RingEntry minimized() {
        return entry(minimized); // entryOf(min());
    }

    /**
     * @return Occupied sectors.
     */
    public final Pattern sectors() {
        return b.or(w);
    }

    public final Pattern sectors(Player player) {

        return switch (player) {
            case Black -> b;
            case White -> w;
            case None -> b.or(w).not();
        };

    }

    /**
     * set up a new player for a given sector.
     * @param sector to set up
     * @param player to set.
     * @return a RingEntry with given player at given sector.
     */
    public final RingEntry withPlayer(Sector sector, Player player) {
        final Player current = player(sector);

        // noop
        if(current==player)
            return this;

        int index = this.index;
        final int pow3 = sector.pow3();
        index -= pow3 * current.ordinal();
        index += pow3 * player.ordinal();

        return entry(index);
    }

    public final RingEntry and(Patterns other) {
        return entry(BW.index(b.and(other.b), w.and(other.w)));
    }

    public final boolean contains(RingEntry other) {
        return b.contains(other.b) && w.contains(other.w);
    }

    protected RingEntry(short index, byte meq, byte mlt, byte pmin, byte mix, short[] perm) {
        super(index);

        assert perm.length == 8 : "invalid perm";
        assert perm[0] == index : "RingEntry: index mismatch";

        this.perm = perm;
        this.inverted = BW.index(w, b);
        this.minimized = perm[mix];

        this.meq = meq;
        this.mlt = mlt;
        this.pmin = pmin;
        this.mix = mix;
    }

    // return short instead of byte to avoid any negative values

    public final String toString() {
        return toString(new StringBuilder()).toString();
    }

    public final  StringBuilder toString(final StringBuilder sb) {

        // prepend pop
        sb.append(String.format("%1d%1d[%1d%1d] ", pop.nb(), pop.nw(), b.mcount, w.mcount));
        pattern(sb);

        // add permutation info
        sb.append(String.format(" ix%d m:%02x e:%02x l:%02x ", mix, pmin(), pmeq(), pmlt()));

        // add permutation group (reversed)
        for(int i=0; i<8; i++)
            sb.append(String.format("%5d", perm[7-i]));

        return sb;
    }

    public final  int hashCode() {
        return index;
    }

    public static final Comparator<RingEntry> COMPARATOR = (o1, o2) -> {
        if(o1==o2)
            return 0;

        if(o1==null)
            return -1;

        return Short.compare(o1.index, o2.index);
    };
}