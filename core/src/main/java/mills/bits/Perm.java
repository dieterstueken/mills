package mills.bits;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.2009
 * Time: 15:59:29
 */

/*
    A permutation is a sequence of mirror (M) and rotate (R) operations.
    For these holds:
    M*M = ID
    R*R*R*R = ID
    M * R^k = R^-k * M;     (with R^-k == R^(4-k))

    Each sequence may be reduced to:
    M^i * R^k with i:[01] and k:[0123]

    Introducing an inversion with X = R*R a sequence may be represented as:

    M^i * X^j * R^k with i,j,k of [0,1]

    Thus there are 8 possible combinations:

    A B
    D C

    rotations:
    ___   R0 identity      ABCD
    __R   R1 rotate  90°   DABC
    _X_   R2 rotate 180°   CDAB
    _XR   R3 rotate 270°   BCDA

    mirrors:
    M__   M0           |   BADC
    M_R   M1 = M * RR  /   ADCB
    MX_   M2 = M * RX  -   DCBA
    MXR   M3 = M * RL  \   CBAD

    The permutation can be applied to a pattern of stones.
*/

public enum Perm implements Operation {

    /**
     * Enum ordinates select operations applied:
     * bit 0: rotate right.
     * bit 1:
     */

    R0, R1, R2, R3,
    M0, M1, M2, M3;

    private final SectMap map = map(ordinal());

    // pre calculated permutations
    private final int composed = composed(ordinal());

    /**
     * @return Return bit mask to use for meq.
     */
    public int msk() {
        return 1<<ordinal();
    }

    /**
     * Apply permutation to a given position mask.
     * The mask may be composed of three 8-bit patterns to represent a full 24-bit position.
     * @param pattern of stones.
     * @return permuted pattern.
     */
    @Override
    public int apply(int pattern) {
        int id = ordinal();

        int result = rotate(pattern, id%4);

        if(id>=4)
            result = mirror(result);

        assert result == map.apply(pattern);

        return result;
    }

    // return inverse operation
    @Override
    public Perm invert() {
        switch(this) {
            case R3: return R1;
            case R1: return R3;
            default:
        }

        // all others are self inverting.
        return this;
    }

    public int compose(int perm) {
        int n = 4*(perm&7);
        return (composed>>>n)&7;
    }

    public Perm compose(final Perm before) {
        return get(compose(before.ordinal()));
    }

    public static int compose(int then, int first) {
        return get(then).compose(first);
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", name(), ordinal());
    }

    /////////////////////// static utilities ///////////////////////

    static SectMap map(int m) {
        SectMap map = SectMap.IDENTITY;

        if((m&1)!=0)
            map = map.andThen(SectMap.ROTATION);

        if((m&2)!=0)
            map = map.andThen(SectMap.INVERSION);

        if((m&4)!=0)
            map = map.andThen(SectMap.MIRRORING);

        return map;
    }

    static int composed(int m) {
        m &= 7;

        // # of rotations
        int mr = m%4;

        for(int k=1; k<8; ++k) {

            // mirrors ? 1 : -1
            int mm = 1 - (k&4)/2;

            // rotation gets possibly mirrored
            int mc = (mm * mr + k) & 3;

            // xor mirrors
            mc |= (m^k)&4;

            // shift up
            mc <<= 4*k;

            // setup
            m |= mc;
        }
        return m;
    }

    static int rotate(int pattern, int count) {
        count &= 3;

        if(count!=0) {
            pattern <<= count;
            int mask = (0x0f << count) & 0xf0;
            mask *= 0x111111;
            mask &= pattern;
            mask ^= mask >>> 4;
            pattern ^= mask;
        }

        return pattern;
    }

    static final int EW = Sector.E.masks();
    static final int NS = Sector.NW.masks() | Sector.SE.masks();

    static int mirror(int pattern) {

        int m = pattern ^ (pattern>>>2);
        m &= EW;
        m |= m<<2;
        pattern ^= m;

        m = pattern ^ (pattern>>>1);
        m &= NS;
        m |= m<<1;
        pattern ^= m;

        return pattern;
    }

    public static final List<Perm> VALUES = List.of(values());

    public static final int MSK = 7;

    // get by index [0,8[
    public static Perm get(int i) { return VALUES.get(i & MSK);}

    // take 4th bit into account to reflect 2:0 swaps
    public static final int SWP = 8;

    // 4 bit mask
    public static final int PERM = 0x0f;
}
