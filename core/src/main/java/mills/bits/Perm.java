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

    public static final List<Perm> VALUES = List.of(values());

    public static final int MSK = 7;

    // get by index [0,8[
    public static Perm get(int i) { return VALUES.get(i & MSK);}

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

    private final SectorOperation op;

    Perm() {
        SectorOperation tmp = SectorOperations.ROTATE.get(nr());
        if(mirrors())
            tmp = tmp.join(SectorOperations.MOP);

        this.op = tmp;
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

    // return composed operation
    public Perm compose(final Perm p) {
        int nr = nr();

        // if his mirrors any rotation goes to the other direction.
        if(mirrors())
            nr += 4 - p.nr();
        else
            nr += p.nr();

        // normalize
        nr &= 3;

        // mirror is an xor of both
        if(mirrors() != p.mirrors())
            return VALUES.get(nr+4);
        else
            return VALUES.get(nr);
    }

    /////////////////////// static utilities ///////////////////////

    // take 4th bit into account to reflect 2:0 swaps
    public static final int SWP = 8;

    // 4 bit mask
    public static final int PERM = 0x0f;

    public static void main(String ... args) {

        for(final Perm p1:Perm.values()) {
            
            for(final Perm p2:Perm.values()) {
                final Perm p3 = p1.compose(p2);
                System.out.format(" %s", p3.name());
            }

            System.out.println();
        }
    }
}
