package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.2010
 * Time: 15:24:21
 */

/**
 * Class BW holds a pair of patterns occupied by black and white stones on a single 8 bit ringTable.
 * All combinations are kept by RING_TABLE.table, so no further instances are needed.
 */
public class BW {

    /**
     * The central index function to map a pair of patterns [0,256[
     * to a composed index of [0, 81*81.
     * @param b black pattern.
     * @param w white pattern.
     * @return a composed ringIndex index.
     */

    public static short index(final Pattern b, final Pattern w) {

        assert (b.pattern&w.pattern) == 0 : "duplicate occupation";

        return (short) (b.pow3* Player.Black.wgt() + w.pow3* Player.White.wgt());
    }

    // There is no essential need to make final primitives private.
    // Instead accessing values directly may improve performance.

    public final Pattern b;
    public final Pattern w;

    public final PopCount pop;

    public final Short index;

    public PopCount pop() {
        return pop;
    }

    public PopCount clop() {
        return PopCount.of(b.mcount, w.mcount);
    }

    public byte nb() {
        return pop.nb();
    }

    public byte nw() {
        return pop.nw();
    }

    protected BW(short index) {

        this.index = index;

        int b = 0;
        int w = 0;

        for(int m=1; index!=0; m*=2, index/=3) {
            int k = index%3;
            if(k== Player.Black.wgt())
                b |= m;
            else
            if(k== Player.White.wgt())
                w |= m;
        }

        this.b = Pattern.of(b);
        this.w = Pattern.of(w);

        pop = PopCount.of(this.b.count(), this.w.count());
    }

    public String pattern() {
        return pattern(new StringBuilder()).toString();
    }

    private static final char[] SIG = {'_', 'X', 'O'};

    public StringBuilder pattern(StringBuilder pattern) {

        int index = this.index;
        for(int i=0; i<8; i++, index/=3) {
            int k = index%3;
            pattern.append(SIG[k]);

            if(i==3)
                pattern.append('|');
        }

        return pattern;
    }

    public static PopCount clop(BW r2, BW r0, BW r1) {
        return PopCount.of(
                Pattern.mcount(r2.b, r0.b, r1.b),
                Pattern.mcount(r2.w, r0.w, r1.w));
    }
}
