package mills.bits;

import static mills.bits.Sector.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 08.09.2010
 * Time: 13:58:03
 */
public abstract class Operation {


    /**
     * Perform operation on a pattern of 8, 16 or 24 bit.
     * @param pattern to permute.
     * @return permuted pattern.
     */

    abstract int apply(int pattern);

    final String name;

    public Operation(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    /**
     * Apply a mask operation on a given pattern.
     * @param mask bits to return.
     * @return an Operation returning a given set of bits.
     */
    static Operation mask(final int mask) {
        return new Operation(String.format("p & 0x%06x", mask)) {

            @Override
            int apply(int pattern) {
                return pattern & mask;
            }
        };
    }

    static Operation mask(final Ring ring) {
        return mask(0xff<<8*ring.ordinal());
    }

    /**
     * Build a Mask operator from a set of sectors.
     * @param sectors to return.
     * @return an Operation returning a given bit set of sectors.
     */
    static Operation mask(final Sector ... sectors) {
        int m = 0;
        for(final Sector s:sectors)
            m |= s.mask();

        // expand the mask repeatedly over three rings.
        m *= 0x010101;

        return mask(m);
    }

    /**
     * Swap the bits represented by two sectors.
     * All other sector bits are cleared.
     * @param s1 first sector ti swap
     * @param s2 second sector to swap
     * @return an Operation returning both sector bits swapped.
     */
    static Operation swap(final Sector s1, final Sector s2) {
        final Operation m1 = mask(s1);
        final Operation m2 = mask(s2);
        final int shift = s2.ordinal()-s1.ordinal();

        return new Operation(String.format("(%s)<<%d | (%s)>>%d", m1, shift, m2, shift)) {

            @Override
            int apply(int pattern) {
                int p1 = m1.apply(pattern) << shift;
                int p2 = m2.apply(pattern) >> shift;
                return p1 | p2;
            }
        };
    }

    public static final Operation ID = new Operation("ID") {

        @Override
        int apply(int pattern) {
            return pattern;
        }

        public Operation join(Operation other) {
            return other;
        }
    };

    /**
     *    NW N NE       NE N NW
     *    W     E  -->  E  |  W
     *    SW S SE       SE S SW
     *
     *    |-1  0|
     *    | 0  1|
     */


    public static final Operation MIRROR = new Operation("MIRROR") {

        final Operation keep_NS = mask(N, S);
        final Operation swapNEW = swap(NW, NE);
        final Operation swap_EW = swap(E, W);
        final Operation swapSEW = swap(SE, SW);

        @Override
        int apply(final int pattern) {
            int result = keep_NS.apply(pattern);
            result |= swapNEW.apply(pattern);
            result |= swap_EW.apply(pattern);
            result |= swapSEW.apply(pattern);
            return result;
        }
    };


    /**
     * NW N NE       SW W NW
     * W     E  -->  S  ↷ N
     * SW S SE       SE E NE
     *
     * | 0 1|
     * |-1 0|
     *
     */

    public static final Operation ROTATE = new Operation("ROTATE") {

        final Operation lower = mask(N,E,S, NW,NE,SE);
        final Operation upper = mask(W, SW);

        @Override
        int apply(final int pattern) {
            int p1 = lower.apply(pattern)<<1;
            int p2 = upper.apply(pattern)>>3;
            return p1 | p2;
        }
    };

    /**
     * Reflection operation (may also be expressed by two rotations)
     *
     * NW N NE       SE S SW
     * W     E  -->  E  X  W
     * SW S SE       NE N NW
     *
     *  |-1 0|
     *  |0 -1|
     */
    
    public static final Operation INVERT = new Operation("INVERT") {

        final Operation lower = mask(N,E, NW,NE);
        final Operation upper = mask(S,W, SE,SW);

        @Override
        int apply(final int pattern) {
            int p1 = lower.apply(pattern)<<2;
            int p2 = upper.apply(pattern)>>2;
            return p1 | p2;
        }
    };

    /**
     * The SWAP Operation acts on whole rings and swaps the outer with the inner ring.
     */
    public static final Operation SWAP = new Operation("SWAP") {
        final Operation outer = mask(Ring.OUTER);
        final Operation middle = mask(Ring.MIDDLE);
        final Operation inner = mask(Ring.INNER);
        final int shift = 8*(Ring.INNER.ordinal() - Ring.OUTER.ordinal());

        @Override
        int apply(int pattern) {
            int m = middle.apply(pattern);
            int o = inner.apply(pattern) >> shift;
            int i = outer.apply(pattern) << shift;
            return m | i | o;
        }
    };

    public static Operation product(Operation op1, Operation op2) {

        if(op2==ID)
            return op1;

        return new Operation(String.format("%s·%s", op1.toString(), op2.toString())) {
            @Override
            int apply(int pattern) {
                pattern = op1.apply(pattern);
                pattern = op2.apply(pattern);
                return pattern;
            }
        };
    }

    public Operation join(Operation other) {
        return product(this, other);
    }

    public static Operation combine(int msk) {
        Operation op = Operation.ID;
        if((msk&8)!=0)
            op = op.join(SWAP);

        if((msk&4)!=0)
            op = op.join(MIRROR);

        if((msk&2)!=0)
            op = op.join(INVERT);

        if((msk&1)!=0)
            op = op.join(ROTATE);

        return op;
    }
}