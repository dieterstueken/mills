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

    ___   ID identity
    __R   RR rotate  90°
    _X_   RL rotate 180°
    _XR   RX rotate 270°

    M__   MH           | mirror
    M_R   MR = M * RR  /
    MX_   ML = M * RL  -
    MXR   MV = M * RX  \

    The permutation will be applied to a pattern of stones.
    8 positions (a single ringTable) are enumerated by two groups of four:

    0 - 1     + 4 +     0 4 1
    |   |  *  7   5  =  7   5
    3 - 2     + 6 +     3 6 2

    Three groups of 8 stones form a complete pattern of three rings.
*/

public enum Perm {

    ID(0), RR(1), RL(3), RX(2),
    MH(4), MR(5), ML(7), MV(6);

    public static final List<Perm> VALUES = List.of(values());

    public static final int MSK = 7;

    // get by index [0,8[
    public static Perm get(int i) { return VALUES.get(i & MSK);}

    private final Operation op;

    private Perm compose[] = new Perm[8];

    Perm(int pm) {
        op = Operation.combine(pm);
    }

    /**
     * Apply permutation to a given position mask.
     * The mask may be composed of three 8-bit patterns to represent a full 24-bit position.
     * @param pattern of stones.
     * @return permuted pattern.
     */
    int apply(int pattern) {
        return op.apply(pattern);
    }

    // return inverse operation
    public Perm inverse() {

        if(this==RL)
            return RR;

        if(this==RR)
            return RL;

        // all others are self inverting.
        return this;
    }

    // return composed operation
    public Perm compose(final Perm p) {
        Perm c = compose[p.ordinal()];

        if(c==null) {
            // try to find inverse using most asymmetric position: 0x11
            int result = p.apply(this.apply(0x11));
            for (Perm px : VALUES) {
                if(px.apply(0x11) == result) {
                    if(c!=null) {
                        throw new RuntimeException(String.format("duplicate composition of %s X %s", name(), p.name()));
                    }
                    c = px;
                }
            }

            if(c==null)
                throw new RuntimeException(String.format("missing composition of %s X %s", name(), p.name()));

            compose[p.ordinal()] = c;
        }

        return c;
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
