package mills.stones;

import mills.bits.Pattern;
import mills.bits.Player;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.12.11
 * Time: 13:41
 */
public enum Mills {

    NR, ER, SR, WR,
    N2, E2, S2, W2,
    N0, E0, S0, W0,
    N1, E1, S1, W1;

    final int closed;

    private static final Mills mills[] = values();

    Mills() {
        this.closed = closed(ordinal());
    }

    public boolean matches(int stones) {
        return (stones& closed)== closed;
    }

    static Mills mills(int index) {
        return mills[index];
    }

    static int closed(int i) {

        if(i<4) // radial mills
            return 0x010101<<i;

        // ring
        int r = (i-4)/4;

        // sector
        int s = i%4;

        int m = Pattern.of(0x31).perm(s).stones();
        m <<= 8*r;

        return m;
    }

    public static int mask(int stones) {
        int mask = 0;

        for(Mills m:mills) {
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
