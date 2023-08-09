package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/29/15
 * Time: 12:56 PM
 */
public class Patterns {

    /**
     * The central index function to map a pair of patterns [0,256[
     * to a composed index of [0, 81*81.
     * @param b black pattern.
     * @param w white pattern.
     * @return a composed ringIndex index.
     */

    public static short index(final Pattern b, final Pattern w) {

        assert (b.pattern&w.pattern) == 0 : "duplicate occupation";
        
        if((b.pattern&w.pattern)!=0)
            throw new IllegalStateException("duplicate occupation");

        return (short) (b.pow3* Player.Black.wgt() + w.pow3* Player.White.wgt());
    }

    public static short index(int b, int w) {
        return index(Pattern.of(b), Pattern.of(w));
    }

    public final Pattern b;
    public final Pattern w;

    public Patterns(Pattern b, Pattern w) {
        this.b = b;
        this.w = w;
    }

    public Patterns(short index) {
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

        assert (b&w) == 0 : "duplicate occupation";

        this.b = Pattern.of(b);
        this.w = Pattern.of(w);
    }

    public short perm(int i) {
        return index(b.perm(i), w.perm(i));
    }
}
