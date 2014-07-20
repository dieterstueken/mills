package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.06.12
 * Time: 22:32
 */

import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * For some reason for a single RingEntry exactly 9 different MEQ permutation masks exist.
 * This Enum provides functions to map from and to a PGroup.
 */
public enum PGroup {

    M01(0x01),
    M11(0x11),
    M21(0x21),
    M81(0x81),
    M41(0x41),
    M05(0x05),
    M55(0x55),
    MA5(0xa5),
    MFF(0xff);

    public final int msk;

    public static PGroup get(int i) {return VALUES[i];}

    public static int size() {return 9;}

    public int msk() {return msk;}

    public int meq() {return 2*msk+1;}

    PGroup(int meq) {
        // drop bit #0
        this.msk = meq/2;
    }

    private static final PGroup VALUES[] = values();
    private static final PGroup GROUP[] = new PGroup[64];

    static {
        // prepare a reverse lookup table
        // GROUP is a sparse table with unexpected masks == null
        // bit #1 is not relevant for distinction.
        for(PGroup p:VALUES) {
            assert GROUP[p.msk/2]==null : "reverse mapping failed";
            GROUP[p.msk/2] = p;
        }
    }

    /**
     * Inverse lookup meq -> PGroup.
     * @param meq of RingEntry.
     * @return matching PGroup.
     */
    public static PGroup group(int meq) {
        PGroup pg = GROUP[meq/4];
        assert pg!=null && pg.msk==(meq/2) : "invalid permutation mask";
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
     * Calculate a partition index for a given restriction mask.
     * @param msk of prohibited meq bits.
     * @return the highest partition index of all permitted bits set.
     */
    public static int pindex(Set<PGroup> groups, int msk) {
        // bits allowed
            int index = 127;

            for(PGroup pg:groups) {

                if ((pg.msk & msk) == 0) {
                    // clear all msk bits from index
                    index &= 127 ^ pg.msk;
                }
            }

            return index;
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
            mask |= pg.msk;
        }

        return mask;
    }

    /**
     * A Predicate to filter RingEntries for a given restriction mask.
     * The restriction mask indicates all bits for which a given RingEntry must NOT be minimized.
     *
     * The Predicate returns if a given RingEntry is compatible to the given mask.
     * The mask will tag permutations for which the i20 candidate will decrease (mlt).
     * The given RingEntry is compatible, if i20 won't decrease for any stable permutation.
     */

    private static final List<Predicate<RingEntry>> FILTER = new ArrayList<>(128);

    public static Predicate<RingEntry> filter(int msk) {
        return FILTER.get(msk);
    }

    static {
        for(int msk=0; msk<128; ++msk) {

            // bit #0 is irrelevant
            int restriction = 2*msk;

            // for each minimizable permutation (pmin) the mask's bit must be zero (= i20 won't decrease).
            FILTER.add(entry -> (entry.pmin() & restriction) == 0);
        }
    }
}
