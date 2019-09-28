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
    ___   ID identity      ABCD
    __R   RR rotate  90°   DABC
    _X_   RX rotate 180°   CDAB
    _XR   RL rotate 270°   BCDA

    mirrors:
    M__   MH           |   BADC
    M_R   MR = M * RR  /   ADCB
    MX_   MV = M * RX  -   DCBA
    MXR   ML = M * RL  \   CBAD

    The permutation can be applied to a pattern of stones.
*/

public enum Perm implements Operation {

    /**
     * Enum ordinates and mask to select operations differ.
     */

    ID, RR, RX, RL,
    MH, MR, MV, ML;

    private final SectorOperation op = op(ordinal());

    // pre calculated permutations
    private final int composed = composed(ordinal());

    // # of rotations
    public int nr() {
        return ordinal()%4;
    }

    // if this operation mirrors
    public boolean mirrors() {
        return (ordinal()&4)!=0;
    }

    /**
     * @return Return bit mask to use for meq.
     */
    public int msk() {
        return 1<<ordinal();
    }

    public SectorOperation op() {
        return op;
    }

    /**
     * Apply permutation to a given position mask.
     * The mask may be composed of three 8-bit patterns to represent a full 24-bit position.
     * @param pattern of stones.
     * @return permuted pattern.
     */
    @Override
    public int apply(int pattern) {
        return op.apply(pattern);
    }

    // return inverse operation
    @Override
    public Perm invert() {
        switch(this) {
            case RL: return RR;
            case RR: return RL;
            default:
        }

        // all others are self inverting.
        return this;
    }

    public int compose(int perm) {
        int n = 4*(perm&7);
        return (composed>>n)&7;
    }

    public Perm compose(final Perm p) {
        return get(compose(p.ordinal()));
    }

    public static int compose(int p0, int p1) {
        return get(p0).compose(p1);
    }

    @Override
    public String toString() {
        return String.format("%s[%d]", name(), ordinal());
    }

    /////////////////////// static utilities ///////////////////////

    static SectorOperation op(int m) {
        SectorOperation op = SectorOperations.ROTATE.get(m&3);
        if((m&4)!=0)
            op = op.join(SectorOperations.MOP);
        return op;
    }

    static int composed(int m) {
        m &= 7;

        // # of rotations
        int mr = m%4;

        // mirrors 1 : -1
        int mm = 1 - (m&4)/2;

        for(int k=1; k<8; ++k) {
            // add positive or negative rotation
            int mc = mr + mm * (k%4);
            mc &= 3;

            // xor mirrors
            mc |= (m^k)&4;

            // shift up
            mc <<= 4*k;

            // setup
            m |= mc;
        }
        return m;
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
