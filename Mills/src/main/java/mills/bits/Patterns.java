package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/29/15
 * Time: 12:56 PM
 */
public class Patterns {

    public final Pattern b;
    public final Pattern w;

    public Patterns(Pattern b, Pattern w) {
        this.b = b;
        this.w = w;
    }

    protected Patterns(short index) {
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

    public static final Patterns NONE = new Patterns(Pattern.NONE, Pattern.NONE);
    public static final Patterns ALL = new Patterns(Pattern.ALL, Pattern.ALL);

    public Patterns and(Patterns other) {
        return new Patterns(b.and(other.b), w.and(other.w));
    }

    public Patterns or(Patterns other) {
        return new Patterns(b.or(other.b), w.or(other.w));
    }

    public Patterns not() {
        return new Patterns(b.not(), w.not());
    }
}
