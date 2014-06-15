package mills.index.partitions;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.09.12
 * Time: 20:18
 */

import mills.bits.Perm;
import mills.ring.RingEntry;

/**
 * Minimize a given i2:i0 combination.
 * return a permutation mask on bit 0:4 and the result by the upper bits.
 */

public class Min20 {

    static final Min20 MIN20 = new Min20("Min20");

    private final String name;

    private Min20(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    /**
     * Return minimized combination i0:02:0 or i2:i0:SWP
     * @param i2 outer ring index
     * @param i0 inner ring index
     * @return combination of both with mask on lower 4 bits.
     */
    public int min20(short i2, short i0) {
        if(i2<i0) {
            // swap i2 into leading part
            return (i2 * RingEntry.MAX_INDEX + i0)<<4 + Perm.SWP;
        } else {
            return (i0 * RingEntry.MAX_INDEX + i2)<<4;
        }
    }

    private static final Min20[] TABLE = new Min20[128];

    public static Min20 get(int msk) {
        return TABLE[msk];
    }

    static {
        for(int msk=0; msk<128; ++msk) {
            int m = Integer.highestOneBit(msk);
        }
    }


    public static Min20 build(final int msk, final int i) {
        if(i<1)
            return MIN20;

        int m = 1<<i;

        if((msk&m)==0)
            return build(msk, i-1);

        final Min20 next = build(msk&(m-1), i-1);

        final String name = String.format("%s:%02x", next.toString(), m);

        return new Min20(name) {
            public int min20(short i2, short i0) {
                int mx = next.min20(i2, i0);

                i2 = RingEntry.of(i2).perm(7);
                i0 = RingEntry.of(i0).perm(7);
                int my = super.min20(i2, i0);
                my |= i;

                return Math.min(mx, my);
            }
        };
    }

    public static Min20 build(int msk) {
        return build(msk, 7);
    }

}
