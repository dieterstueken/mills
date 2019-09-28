package mills.bits;

import java.util.function.IntUnaryOperator;
import java.util.function.UnaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.09.19
 * Time: 19:39
 */
public class SectMap implements Operation {

    public static final SectMap IDENTITY = ofi(IntUnaryOperator.identity());
    public static final SectMap ROTATION = of(Sector::rotation);
    public static final SectMap INVERSION = of(Sector::inversion);
    public static final SectMap MIRRORING = of(Sector::mirror);

    private final int map;

    public SectMap(int map) {
        this.map=map;
    }

    public static SectMap of(UnaryOperator<Sector> op) {
        int map = 0;
        int seen = 0;

        for (Sector s : Sector.SECTORS) {
            int ms = op.apply(s).ordinal();
            map |= ms << (4*s.ordinal());
            seen |= 1<<ms;
        }

        if(seen!=0xff)
            throw new IllegalArgumentException("invalid SectMap");

        return new SectMap(map);
    }

    public static SectMap ofi(IntUnaryOperator op) {
        return of(s -> Sector.of(op.applyAsInt(s.ordinal())));
    }

    public Sector get(Sector s) {
        return Sector.of(get(s.ordinal()));
    }

    public int get(int is) {
        int k = map>>>(4*is);
        return k&7;
    }

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

    /**
     * Returns a composed operator that first applies the {@code before}
     * operator to its input, and then applies this operator to the result.
     * If evaluation of either operator throws an exception, it is relayed to
     * the caller of the composed operator.
     *
     * @param before the operator to apply before this operator is applied
     * @return a composed operator that first applies the {@code before}
     * operator and then applies this operator
     * @throws NullPointerException if before is null
     */
    
    public SectMap compose(IntUnaryOperator before) {
        return ofi(before.andThen(this::get));
    }

    public SectMap compose(SectMap before) {
        return compose(before::get);
    }

    public SectMap andThen(SectMap before) {
        return before.compose(this);
    }

    public SectMap invert() {
        int result = 0;

        for(int i=0; i<8; ++i) {
            int is = get(i);
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