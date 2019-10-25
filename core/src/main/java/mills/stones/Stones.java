package mills.stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 14.08.11
 * Time: 11:01
 */


import mills.bits.BW;
import mills.bits.Pattern;
import mills.bits.Perm;
import mills.bits.Player;
import mills.position.Positions;

/**
 * Class Stones manages a bit mask of 24 stones.
 *
 *
 *  bit numbers of position (middle ring occupies highest bits)
 *
 *   8-----12------9
 *   | 16--20--17  |
 *   |  | 0-4-1 |  |
 *  15-23-7   5-21-13
 *   |  | 3-6-2 |  |
 *   | 19--22--18  |
 *  11-----14-----10

 *  bit patterns
 *
 *      01 10 02
 *      80    20
 *      08 40 04
 *
*/
public class Stones {

    public static final int STONES = 0xffffff;

    public Stones(int stones) {
        this.stones = stones;
    }

    final int stones;

    public static boolean equals(int m1, int m2) {
        int m = (m1 ^ m2) & STONES;
        return m == 0;
    }

    // build stones, assume all values are in range [0,256[
    public static int stones(int i2, int i0, int i1) {
        assert (i1&0xff)==i1;
        assert (i2&0xff)==i2;
        assert (i0&0xff)==i0;

        return (i1<<16) | (i0<<8) | i2;
    }

    public static int stones(long i201, Player player) {
        int i2 = player.stones(Positions.r2(i201));
        int i0 = player.stones(Positions.r0(i201));
        int i1 = player.stones(Positions.r1(i201));
        return stones(i2, i0, i1);
    }

    static Pattern p2(int stones) {return Pattern.of(stones);}
    static Pattern p0(int stones) {return Pattern.of(stones>>>8);}
    static Pattern p1(int stones) {return Pattern.of(stones>>>16);}

    public static short i2(int black, int white) {return BW.index(p2(black), p2(white));}
    public static short i0(int black, int white) {return BW.index(p0(black), p0(white));}
    public static short i1(int black, int white) {return BW.index(p1(black), p1(white));}

    public static long i201(int black, int white) {

        int i2 = i2(black, white);
        int i0 = i0(black, white);
        int i1 = i1(black, white);

        return Positions.i201(i2, i0, i1);
    }

    static int perm(final int stones, final int perm) {
        int i2 = p2(stones).perm(perm).stones();
        int i0 = p0(stones).perm(perm).stones();
        int i1 = p1(stones).perm(perm).stones();

        if((perm& Perm.SWP)==0)
            return stones(i2, i0, i1);
        else
            return stones(i0, i2, i1);
    }

    public static int free(int stones) {
        return stones ^ STONES;
    }

    public static int closed(final int stones) {

        Pattern p2 = p2(stones);
        Pattern p0 = p0(stones);
        Pattern p1 = p1(stones);

        int radials = Pattern.radials(p2, p0, p1);
        int closed = stones(p2.closed(), p0.closed(), p1.closed());

        return radials | closed;
    }

    public static int closes(final int stones) {

        Pattern p2 = p2(stones);
        Pattern p0 = p0(stones);
        Pattern p1 = p1(stones);

        int closes = stones(p2.closes(p0, p1), p0.closes(p1, p2), p1.closes(p0, p2));

        return closes;
    }
}
