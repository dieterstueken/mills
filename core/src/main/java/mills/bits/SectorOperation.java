package mills.bits;

import java.util.EnumMap;
import java.util.function.UnaryOperator;

import static mills.bits.Sector.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 08.09.2010
 * Time: 13:58:03
 */
public interface SectorOperation extends Operation {

    /**
     * Each sector is mapped to a target position.
     * @param s sector to map.
     * @return target position mapped to.
     */
    Sector map(Sector s);

    /**
     * Perform operation on a pattern of 8, 16 or 24 bit.
     * @param pattern to permute.
     * @return permuted pattern.
     */

    default int apply(int pattern) {
        int result = 0;

        for(int i=0; i<8; ++i) {
            Sector s = SECTORS.get(i);
            int bits = pattern & s.masks();
            if(bits!=0) {
                Sector t = map(s);
                int is = t.ordinal() - i;
                if(is<0)
                    result |= bits>>>-is;
                else
                    result |= bits<<is;
            }
        }

        assert Integer.bitCount(pattern) == Integer.bitCount(result);

        return result;
    }

    default SectorOperation invert() {
        SectorOperation parent = this;

        UnaryOperator<Sector> map = invert(this::map);
        String name = "-" + toString();

        return new SectorOperations(name, map) {

            @Override
            public SectorOperation invert() {
                return parent;
            }
        };
    }

    default SectorOperation join(SectorOperation other) {
        String name = toString() + '·' + other.toString();
        return new SectorOperations(name, join(this::map, other::map));
    }

    static boolean verify(UnaryOperator<Sector> map) {
        int mask = 0;
        for (Sector s : SECTORS) {
            Sector t = map.apply(s);
            mask |= t.mask();
        }

        // 8 bits set
        assert mask==0xff;
        return true;
    }

    static UnaryOperator<Sector> invert(UnaryOperator<Sector> map) {
        EnumMap<Sector, Sector> inv = new EnumMap<>(Sector.class);

        for (Sector s : SECTORS) {
            Sector m = map.apply(s);
            inv.put(m, s);
        }

        return inv::get;
    }

    static UnaryOperator<Sector> join(UnaryOperator<Sector> a, UnaryOperator<Sector> b) {
        EnumMap<Sector, Sector> map = new EnumMap<>(Sector.class);

        for (Sector s : SECTORS) {
            Sector m = a.apply(s);
            m = b.apply(m);
            map.put(s, m);
        }

        return map::get;
    }

    static boolean equals(UnaryOperator<Sector> a, UnaryOperator<Sector> b) {
        for (Sector s : SECTORS) {
            if(a.apply(s) != b.apply(s))
                return false;
        }
        return true;
    }

    /**
     *    mirror map
     *
     *    NW N NE       NE N NW
     *    W     E  -->  E  |  W
     *    SW S SE       SE S SW
     *
     *    |-1  0|
     *    | 0  1|
     */

    static UnaryOperator<Sector> mirror() {
        EnumMap<Sector, Sector> map = new EnumMap<>(Sector.class);

        map.put(N, N);
        map.put(E, W);
        map.put(S, S);
        map.put(W, E);

        map.put(NW, NE);
        map.put(NE, NW);
        map.put(SE, SW);
        map.put(SW, SE);

        return map::get;
    }

    /**
     * rotate right map
     *
     * NW N NE       SW W NW
     * W     E  -->  S  ↷ N
     * SW S SE       SE E NE
     *
     * | 0 1|
     * |-1 0|
     *
     */


    static UnaryOperator<Sector> rotate() {
        EnumMap<Sector, Sector> map = new EnumMap<>(Sector.class);

        map.put(N, E);
        map.put(E, S);
        map.put(S, W);
        map.put(W, N);

        map.put(NW, NE);
        map.put(NE, SE);
        map.put(SE, SW);
        map.put(SW, NW);

        return map::get;
    }

    UnaryOperator<Sector> ROP = rotate();
    UnaryOperator<Sector> XOP = join(ROP, ROP);
    UnaryOperator<Sector> LOP = join(XOP, ROP);

    UnaryOperator<Sector> MOP = mirror();
}
