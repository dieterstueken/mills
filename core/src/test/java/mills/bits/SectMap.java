package mills.bits;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.09.19
 * Time: 19:39
 */

/**
 * Class SectMap provides a mapping of Sector[8] -> Sector[8].
 * The mapping is stored by 4x4 bits of an int.
 * Mappings can be chained and inverted.
 *
 * This is an alternate implementation to Perm.
 */
public class SectMap implements UnaryOperator<Sector>, Operation {

    private final int map;

    private SectMap(int map) {
        this.map=map;
    }

    // Since a sector index occupies only 3 bit, the 4.th bit can be used temporary as an marker.
    private static final int MARKER = 0x88888888;

    public static SectMap of(Function<Sector, Sector> op) {
        int map = 0;

        for (Sector s : Sector.SECTORS) {
            int isec = op.apply(s).index();
            isec |= 8; // use unused 4th bit as maker flag
            map |= isec << (4*s.index());
        }

        // this must reset all markers if all set.
        map ^= MARKER;

        if((map&MARKER)!=0)
            throw new IllegalArgumentException("invalid SectMap");

        return new SectMap(map);
    }

    public SectMap composed(Function<Sector, Sector> before) {
        return SectMap.of(before.andThen(this));
    }

    /**
     * Map a sector index to its target index.
     * @param isec input sector index.
     * @return mapped sector index.
     */
    public int map(int isec) {
        int k = map>>>(4*isec);
        return k&7;
    }

    /**
     * Map a single Sector to its target position.
     * @param sector to map.
     * @return target Sector.
     */
    @Override
    public Sector apply(Sector sector) {
        int isec = sector.index();
        isec = map(isec);
        return Sector.of(isec);
    }

    /**
     * Apply mapping on a bit pattern of 3*8 stones.
     * @param stones to map.
     * @return mapped stone pattern.
     */
    public int apply(int stones) {
        int result = 0;

        if((stones&0xff)!=stones) {
            result = apply(stones>>>8)<<8;
            stones &= 0xff;
        }

        for(int sectors = map; stones!=0; stones >>>=1, sectors>>>=4) {

            if((stones&0x0f)==0) {
                stones >>>= 4;
                sectors >>>= 16;
            }

            if((stones&0x03)==0) {
                stones >>>= 2;
                sectors >>>= 8;
            }

            if((stones&1)!=0)
                result |= 1<<(sectors&7);
        }

        return result;
    }

    public SectMap invert() {
        int result = 0;

        for(int i=0; i<8; ++i) {
            int is = map(i);
            result |= i<<(4*is);
        }

        return new SectMap(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SectMap sectMap = (SectMap) o;
        return map == sectMap.map;
    }

    @Override
    public int hashCode() {
        return map;
    }
}