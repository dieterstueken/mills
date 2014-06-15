package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.06.12
 * Time: 22:32
 */

import com.google.common.base.Predicate;
import mills.ring.RingEntry;

import java.util.ArrayList;
import java.util.List;

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

    /**
     * A Set of PGroups to calculate restricted / permitted bits.
     */
    public static class Set {

        final int groups;

        private Set(int groups) {this.groups = groups;}

        public static Set of(final List<RingEntry> entries) {

            int groups = 0;

            // sum up all groups.
            for(final RingEntry e : entries)
                groups |= 1 << e.grp.ordinal();

            return new Set(groups);
        }

        /**
         * Calculate a partition index for a given restriction mask.
         * @param restriction mask of prohibited meq bits.
         * @return the highest partition index of all permitted bits set.
         */
        public int partition(int restriction) {

            // sum up all restricted bits.
            int sum = 0;

            for(int i=0; i<9; ++i) {
                if((groups&(1<<i)) != 0) {
                    final PGroup pg = PGroup.get(i);
                    final int msk = pg.msk;
                    // sum up all permitted groups
                    if((msk&restriction) == 0)
                        sum |= msk;
                }
            }

            // the partition index has all bits set which are still unset.
            return sum ^ 127;
        }
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
