package mills.bits;

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

    Thus there are 8 possible combinations:

    R0  identity
    R1  rotate  +90
    R2  rotate +180
    R3  rotate +270

    M0  \ mirror
    M1  | M * R1
    M2  / M * R2
    M3  - M * R3

    given three bits i,j,k a permutation may be expressed as:

    P = M^i * R2^j * R1^k

    The permutation will be applied to a pattern of stones.
    8 positions (a single ringTable) are enumerated by two groups of four:

    0 - 1     + 4 +     0 4 1
    |   |  *  7   5  =  7   5
    3 - 2     + 6 +     3 6 2

    Three groups of 8 stones form a complete pattern of three rings.
*/

public enum Perm {

    R0,R1,R2,R3, M0,M1,M2,M3;

    private static final Perm VALUES[] = values();

    public static int MSK = 7;

    // get by index [0,8[
    public static Perm get(int i) { return VALUES[i&MSK];}

    // generate from operations
    static Perm get(boolean mirror, int rotations) {
        int i = mirror?4:0;
        i += rotations&3;
        return get(i);
    }

    byte perm() {
        return (byte) ordinal();
    }

    /**
     * Apply permutation on a given position mask.
     * The mask may be composed of three 8-bit patterns to represent a full 24-bit position.
     * @param pattern of stones.
     * @return permuted pattern.
     */
    int apply(int pattern) {

        int i = ordinal();

        if((i&4)!=0)
            pattern = Operation.MIRROR.apply(pattern);

        if((i&2)!=0)  // reflection is equivalent to R2
            pattern = Operation.REFLECT.apply(pattern);

        if((i&1)!=0)
            pattern = Operation.ROTATE.apply(pattern);

        return pattern;
    }

    // return # of rotations [0,4[
    public int rotations() {
        return ordinal()&7;
    }

    // return if operation performs mirroring
    public boolean mirrors() {
        return (ordinal()&4) != 0;
    }

    // return inverse operation
    public Perm inverse() {
        if(mirrors())
            return this;
        else
            return get(false, -rotations());
    }

    // return composed operation
    public Perm compose(final Perm p) {
        if(p.mirrors())
            return get(!mirrors(), p.rotations() - rotations());
        else
            return get(mirrors(), p.rotations() + rotations());
    }

    /////////////////////// static utilities ///////////////////////

    // take 4th bit into account to reflect 2:0 swaps
    public static int SWP = 8;

    // 4 bit mask
    public static int PERM = 0x0f;

    // lookup tables
    private static final byte composed[] = new byte[64];
    private static final byte inversed[] = new byte[8];

    // compose a 8*8 index
    static int p12(int p1, int p2) {
        return (p1&MSK)|((p2&MSK)*SWP);
    }

    static {
        for(int i=0; i<64; ++i) {
            final Perm p1 = get(i&MSK);
            final Perm p2 = get((i*SWP)&MSK);
            composed[i] = (byte) p1.compose(p2).ordinal();
        }

        for(int i1=0; i1<8; ++i1) {
            final Perm p1 = get(i1);
            inversed[i1] = (byte) p1.inverse().ordinal();

            for(int i2=0; i2<8; ++i2) {
                final Perm p2 = get(i2);
                final Perm px = p1.compose(p2);
                composed[p12(i1,i2)] = px.perm();
            }
        }
    }

    public static byte compose(int p1, int p2) {
        int px = (p1&MSK)|((p2&MSK)<<3);
        byte bx = composed[px];     // lookup combination
        bx |= (p1^p2)& SWP;         // apply swap
        return bx;
    }

    public static int inverse(int p) {
        int px = inversed[(p&MSK)];
        px ^= p&SWP;
        return px;
    }

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
