package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.2010
 * Time: 15:43:39
 */

import com.google.common.collect.ImmutableList;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * class Pattern represents the occupied positions on a ringTable of 8 positions.
 * The 256 possible combinations are kept in a static lookup table.
 * Thus no new objects have to be created.
 * Each Pattern object also keeps all possible permutations of itself in a pre calculated lookup array.
 */
public class Pattern {

    // the pattern is composed of 8 bytes of permuted pattern
    public final long patterns;
    public final short pow3;

    public final byte pattern;
    public final byte meq;
    public final byte count;
    public final byte closed;
    public final byte closes;
    public final byte mcount;

    // return the bit mask of occupied positions
    public int stones() {
        return (0xff & pattern);
    }

    // return the count of occupied bits.
    public int count() {
        //return Integer.bitCount(stones());
        return count;
    }

    public int closed() {
        return 0xff & closed;
    }

    public int closes() {
        return 0xff & closes;
    }

    public int closes(Pattern pa, Pattern pb) {

        int radials = pa.pattern & pb.pattern & (pattern^Sector.RADIALS);
        radials &= pattern^Sector.RADIALS;

        return 0xff & (closes | radials);
    }

    /**
     * Compute closed radial mills. The explicit sequence of patterns is irrelevant.
     * @param p2 outer pattern
     * @param p0 inner pattern
     * @param p1 middle pattern
     * @return a pattern of closed radial mills.
     */
    public static int radials(Pattern p2, Pattern p0, Pattern p1) {

        // find radial mills first
        int radials = (int) (p2.pattern & p0.pattern & p1.pattern & Sector.RADIALS);
        // expand single bits into three radial mills again
        radials *= 0x010101;

        return radials;
    }

    public Pattern and(Pattern other) {
        int stones = pattern & other.pattern;
        return of(stones);
    }

    public Pattern or(Pattern other) {
        int stones = pattern | other.pattern;
        return of(stones);
    }

    public Pattern radials() {
        return and(RADIALS);
    }

    /**
     * Count # of closed mills
     * @param p2 outer pattern
     * @param p0 inner pattern
     * @param p1 middle pattern
     * @return # of closed mills
     */
    public static int mcount(Pattern p2, Pattern p0, Pattern p1) {
        int radials = (p2.pattern & p0.pattern & p1.pattern & Sector.RADIALS);

        int mcount = Sector.N.getBit(radials)
                + Sector.S.getBit(radials)
                + Sector.W.getBit(radials)
                + Sector.E.getBit(radials)
                + p2.mcount + p0.mcount + p1.mcount
                ;

        return mcount;
    }

    public int meq() {
        return meq;
    }

    // return permutation #i
    public Pattern perm(int i) {
        int perm = (int) (0xff & (patterns>>8*i));
        return of(perm);
    }

    /**
     * Map each bit to a power of 3(9, 27, 81, 243, ...).
     * @return accumulated results.
     */
    public short pow3() {
        return pow3;
    }

    public static short pow3(int pattern) {
        short result = 0;
        for(short i3=1; pattern!=0; pattern>>>=1, i3*=3)
            if((pattern&1)==1)
                result += i3;

        return result;
    }

    private static final char SIG[] = {'_', '+', '?', 'x'};

    public String toString() {
        StringBuilder sb = new StringBuilder(32);


        int stones = stones();
        int closed = closed();

        sb.append(String.format("[%02x] ", stones));

        for(int i=0; i<8; i++, stones/=2, closed/=2) {
            int k = (stones&1) + 2*(closed&1);
            sb.append(SIG[k]);

            if(((i%8)==3))
                sb.append('|');
        }

        return sb.toString();
    }

    ///////////////////////////////////////////////////////////
    // there is no need ever to create any further Bit objects
    private Pattern(long pattern, byte meq, byte closed, byte closes, byte mcount) {
        this.patterns = pattern;
        this.pattern = (byte) pattern;

        int stones = (int) (0xff & pattern);
        this.pow3 = pow3(stones);
        this.count = (byte) Integer.bitCount(stones);

        this.meq = meq;
        this.closed = closed;
        this.closes = closes;
        this.mcount = mcount;
    }

    public static Pattern of(int i) {
        return PATTERNS.get(i&0xff);
    }

    // a pre calculated list of all 256 Pattern
    public static final List<Pattern> PATTERNS = ImmutableList.copyOf(patterns());

    public static final Pattern NONE = of(0);
    public static final Pattern RADIALS = of(Sector.RADIALS);
    public static final Pattern ALL = of(0xff);

    private static List<Pattern> patterns() {

        return new AbstractRandomList<Pattern>() {

            final int mills[] = Sector.mills();

            @Override
            public Pattern get(final int i) {
                long pattern = 0;
                byte meq = 0;

                for(int p=0; p<8; p++) {

                    long perm = Perm.get(p).apply(i);
                    pattern |= perm<<(8*p);

                    if(perm==i)
                        meq |= 1<<p;
                }

                byte closed = 0;
                byte closes = 0;
                byte mcount = 0;

                for(int m:mills) {
                    int c = m&i;
                    if(c!=0 && c == m) {// if all thee bits are set
                        closed |= m;    // mark all of them as closed
                        ++mcount;
                    } else if(Integer.bitCount(c)==2) {
                                        // one of three bits is zero
                        closes |= m^c;  // accumulate as closing candidate
                    }
                }

                return new Pattern(pattern, meq, closed, closes, mcount);
            }

            @Override
            public int size() {
                return 256;
            }
        };
    }
}
