package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.06.12
 * Time: 22:32
 */

import com.google.common.collect.ImmutableList;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static mills.bits.Perm.*;

/**
 * For some reason for a single RingEntry exactly 9 different MEQ permutation masks exist.
 * This Enum provides functions to map from and to a PGroup.
 */
public enum PGroup {

    // no symmetry at all
    P0(ID),   // 5616:702

    // mirror one axis (54)
    P1(ID, MH), // 216:54
    P2(ID, MR), // 216:96
    P3(ID, ML), // 216:54
    P4(ID, MV), // 216:12

    // central point mirror (9)
    P5(ID, RX), // 36:9

    // central point plus and two mirror axis
    P6(ID, RX, MH, MV), // 18:9
    P7(ID, RX, MR, ML), // 18:9

    // fully symmetric
    P8(Perm.values());  // 9:9

    public final int meq;

    public int msk() {return meq/2;}

    public int meq() {return meq;}

    /**
     * Return colliding bits of given permutation mask.
     * @param msk of permutations (mlt20/2).
     * @return mask of colliding bits.
     */
    public int collisions(int msk) {
        return msk & this.msk();
    }

    /**
     * Find if any of the permutations of msk (mlt20/2) collides with any permutation of this group.
     * @param msk of permutations (mlt20/2).
     * @return if any permutation of perm collides with any permutation of this.
     */
    public boolean collides(int msk) {
        return collisions(msk) != 0;
    }

    PGroup(Perm ... pg) {
        int m = 0;
        for(Perm p:pg)
            m |= 1<<(p.ordinal());
        this.meq = m;
    }

    public String toString() {
        return String.format("%s(%x)", name(), meq);
    }

    public static final List<PGroup> VALUES = ImmutableList.copyOf(values());

    private static final PGroup GROUP[] = new PGroup[256/8];

    static {
        // prepare a reverse lookup table
        // GROUP is a sparse table with unexpected masks == null
        // bit #0-2 are not relevant for distinction.
        for(PGroup p:VALUES) {
            int k = p.meq/8;
            assert GROUP[k]==null : "duplicate mapping";
            GROUP[k] = p;
        }
    }

    /**
     * Inverse lookup meq -> PGroup.
     * @param meq of RingEntry.
     * @return matching PGroup.
     */
    public static PGroup group(int meq) {
        meq &= 0xff;

        // lower bits #0-2 are irrelevant for distinction.
        PGroup pg = GROUP[meq/8];

        // verify
        if(pg==null || pg.meq!=meq)
            throw new IllegalArgumentException("no matching PGroup");

        return pg;
    }

    public static EnumSet<PGroup> groups(final Iterable<RingEntry> entries) {

        EnumSet<PGroup> groups = EnumSet.noneOf(PGroup.class);
        for(final RingEntry e : entries) {
            groups.add(e.grp);
        }

        return groups;
    }

    /**
     * Calculate maximum partition index for a given restriction mask.
     * @param msk of volatile bits (1=reducing permutation).
     * @return the highest partition index with all permitted bits set.
     */
    public static int pindex(Set<PGroup> groups, int msk) {
        // bits allowed
        int index = 127;

        for(PGroup pg:groups) {

            if ((pg.msk() & msk) == 0) {
                // clear all msk bits from index
                index &= 127 ^ pg.msk();
            }
        }

        return index;
    }

    /**
     * Calculate lowest index for a given restriction mask.
     * @param groups occurring permutation groups.
     * @param msk given mlt mask.
     * @return lowest relevant mask filtering the same permutations.
     */
    public static int lindex(Set<PGroup> groups, int msk) {
        // or ing all collisions.
        return groups.stream()
                .mapToInt(pg->pg.collisions(msk))
                .reduce(0, (a,b) -> a|b);
    }

    public static int code(Set<PGroup> groups) {
        int code = 0;

        for(PGroup pg:groups) {
            int m = 1<<pg.ordinal();
            code |= m;
        }

        return code;
    }

    public static int mask(Set<PGroup> groups) {
        int mask = 0;

        for(PGroup pg:groups) {
            mask |= pg.msk();
        }

        return mask;
    }

    public static void main(String ... args) {

        VALUES.forEach(
                pg->{
                    EntryTable t = RingEntry.TABLE.filter(e->e.grp==pg);
                    System.out.format("%s: %2d\n", pg.toString(), t.size());
                }
        );
    }

    /**
     * A Predicate to filter RingEntries for a given restriction mask.
     * The restriction mask indicates all bits for which a given RingEntry must NOT be minimized.
     *
     * The Predicate returns if a given RingEntry is compatible to the given mask.
     * The mask will tag permutations for which the i20 candidate will decrease (mlt).
     * The given RingEntry is compatible, if i20 won't decrease for any stable permutation.
     */

    //public static final List<Predicate<RingEntry>> FILTERS = AbstractRandomArray.generate(128, msk-> e -> e.stable(2*msk));
}
