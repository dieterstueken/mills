package mills.bits;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.09.2010
 * Time: 20:04:09
 */

/**
 * Class Sector enumerates the 8 possible positions on a single ring.
 * The group is internally divided into 4 positions on the edge and 4 positions at the corners.
 * This finally helps to find closed mill patterns.
 *
 * The ordinal() also defines the bit position to map a group of partitions to an int.
 * To display a Sector each Sector provides some kind if a coordinate (x,y).
 *
 */
public enum Sector {

    /**
     *    Sector     bit      hex
     *    NW N NE   4 0 5   10 01 20
     *    W     E   3   1   08    02
     *    SW S SE   7 2 6   80 04 40
     *
     */
    N(1,0),  E(2,1),  S(1,2),  W(0,1),
    NW(0,0), NE(2,0), SE(2,2), SW(0,2);

    final short x, y, pow3;

    Sector(int x, int y) {
        this.x = (short) x;
        this.y = (short) y;
        this.pow3 = (short) pow3(ordinal());
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    // mask to apply on an int to extract Sector bits
    public int mask() {
        return 1<<ordinal();
    }

    // masks to apply on an int(24) to extract Sector bits
    public int masks() {
        return 0x010101<<ordinal();
    }

    /**
     * Return 0 or 1 from bit mask.
     * @param mask to extract from.
     * @return addressed bit from mask.
     */
    public int getBit(int mask) {
        return (mask>>ordinal())&1;
    }

    public Sector rotate(int i) {
        i += ordinal();
        i &= 3;
        i |= (ordinal()&4);

        return SECTORS.get(i);
    }

    public short pow3() {
        return pow3;
    }

    public static final List<Sector> SECTORS = List.of(values());
    public static final List<Sector> EDGES = List.of(N,E,S,W);
    public static final List<Sector> CORNERS = List.of(NW,NE,SE,SW);

    // members of radial mills
    public static final int RADIALS = group(N,E,S,W);

    public static int[] mills() {
        return new int[] {
                group(NW, N ,NE),
                group(NE, E ,SE),
                group(SE, S ,SW),
                group(SW, W ,NW)
        };
    }

    public static int[] moves() {
        return new int[] {
                group(N, NW),
                group(N ,NE),

                group(E, NE),
                group(E, SE),

                group(S, SE),
                group(S, SW),

                group(W, SW),
                group(W, NW)
        };
    }

    public static Sector of(int i) {
        return SECTORS.get(i);
    }

    // generator function
    public static int pow3(int i) {
        assert i>=0;
        if(i==0)
            return 1;
        else
            return 3*pow3(i-1);
    }

    public static int group(final Sector ... sectors) {
        int m = 0;

        for(final Sector s:sectors)
            m |= s.mask();

        return m;
    }
}
