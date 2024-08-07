package mills.bits;

import mills.util.Indexed;
import mills.util.listset.DirectListSet;

import java.util.function.UnaryOperator;

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

    Thus, there are 8 possible combinations:

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

public enum Perm implements UnaryOperator<Sector>, Operation, Indexed {

    /**
     * Enum ordinates select operations applied:
     * bit 0: rotate right.
     * bit 1:
     */

    R0, R1, R2, R3,
    M0, M1, M2, M3;

    public static final int ROT = 1;
    public static final int INV = 2;
    public static final int MIR = 4;
    public static final int MSK = 7;

    // pre calculated permutations
    private final int composed = composed(ordinal());

    /**
     * @return Return bit mask to use for meq.
     */
    public int msk() {
        return 1<<ordinal();
    }

    public short perm() {
        return (short) ordinal();
    }

    /**
     * @return number of right rotations performed.
     */
    public int rotates() {
        return ordinal()%4;
    }

    /**
     * @return if mirroring takes place.
     */
    public boolean mirrors() {
        return (ordinal()&MIR)!=0;
    }

    /**
     * Apply permutation to a given position mask.
     * The mask may be composed of three 8-bit patterns to represent a full 24-bit position.
     * @param pattern of stones.
     * @return permuted pattern.
     */
    @Override
    public int apply(int pattern) {

        int result = rotate(pattern, rotates());

        if(mirrors())
            result = mirror(result);

        return result;
    }

    /**
     * Implement the Sector -> Sector mapping.
     * @param sector to map.
     * @return mapped sector.
     */
    @Override
    public Sector apply(Sector sector) {
        sector = sector.rotate(rotates());

        if(mirrors())
            sector = sector.mirror();

        return sector;
    }

    // return inverse operation
    @Override
    public Perm invert() {
        return switch (this) {
            case R3 -> R1;
            case R1 -> R3;
            // all others are self inverting.
            default -> this;
        };
    }

    /**
     * Apply this permutation on a given perm leaving other bits untouched.
     * @param perm previous permutation.
     * @return composed permutation.
     */
    public int compose(int perm) {
        int n = 4*(perm&MSK);

        // cut off new permutation
        return (composed>>>n)&MSK;
    }

    public Perm compose(Perm before) {
        return get(compose(before.ordinal()));
    }

    /**
     * Compose p0 and p1 preserving additional flags from p0.
     * @param p0 current permutation.
     * @param p1 additional permutation.
     * @return composition of p0 * p1
     */
    public static short compose(short p0, int p1) {
        int perm = p0&MSK;
        p0 ^= perm; // clear current perm bits
        p0 |= get(perm).compose(p1);
        return p0;
    }

    /**
     * Invert the permutation bits of a status code
     * @param stat status code
     * @return permuted status code
     */
    public static short invert(short stat) {
        // is R1 | R3
        if((stat&5)==1)
            stat ^= 2;

        return stat;
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", name(), ordinal());
    }

    /////////////////////// static utilities ///////////////////////


    private static int composed(int m) {
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

    private static int rotate(int pattern, int count) {
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

    private static final int MEDGES = Sector.E.masks();
    private static final int MCORNERS = Sector.NW.masks() | Sector.SE.masks();

    private static int mirror(int pattern) {

        int mask = pattern ^ (pattern>>>2);
        mask &= MEDGES;
        mask |= mask<<2;
        pattern ^= mask;

        mask = pattern ^ (pattern>>>1);
        mask &= MCORNERS;
        mask |= mask<<1;
        pattern ^= mask;

        return pattern;
    }

    public static final DirectListSet<Perm> VALUES = DirectListSet.of(values());

    // get by index [0,8[
    public static Perm get(int i) { return VALUES.get(i & Perms.MSK);}

    @Override
    public int getIndex() {
        return ordinal();
    }
}
