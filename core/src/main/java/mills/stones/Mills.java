package mills.stones;

import mills.bits.Player;
import mills.bits.Sector;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.12.11
 * Time: 13:41
 */

/**
 * Each entry represents a stone mask of closed mills.
 */
public enum Mills {

    NR, ER, SR, WR, // radial mills
    N2, E2, S2, W2, // edges on r2
    N0, E0, S0, W0, // edges on r0
    N1, E1, S1, W1; // edges on r1

    final int closed;

    public static final List<Mills> MILLS = List.of(values());

    Mills() {
        this.closed = closed(ordinal());
    }

    public boolean matches(int stones) {
        return (stones& closed)== closed;
    }

    static Mills mills(int index) {
        return MILLS.get(index);
    }

    static int closed(int i) {

        if(i<4) // radial mills
            return 0x010101 * Sector.EDGES.get(i).mask();

        // sector
        int k = i%4;

        // edge and neighboring corners
        int m = Sector.CORNERS.get(k).mask();
        m |= Sector.EDGES.get(k).mask();
        m |= Sector.CORNERS.get((k+1)%4).mask();

        // ring
        int l = (i-4)/4;
        m <<= 8*l;

        return m;
    }

    /**
     * Return a bit mask (like enum set) of closed mills [0,16[
     * @param stones to analyze
     * @return mill mask
     */
    public static int mask(int stones) {
        int mask = 0;

        for(Mills m:MILLS) {
            if(m.matches(stones))
                mask |= 1<<m.ordinal();
        }

        return mask;
    }

    public static int count(int stones) {
        return Integer.bitCount(mask(stones));
    }

    public static int count(long i201, Player player) {
        int stones = Stones.stones(i201, player);
        return Integer.bitCount(mask(stones));
    }
}
