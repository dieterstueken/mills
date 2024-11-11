package mills.bits;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.2010
 * Time: 15:24:21
 */

import mills.util.Indexed;

/**
 * Class BW holds a pair of patterns occupied by black and white stones on a single 8 bit ringTable.
 * All combinations are kept by RING_TABLE.table, so no further instances are needed.
 */
public class BW extends Patterns implements Indexed, Comparable<BW> {

    public final PopCount pop;

    public final Short index;

    // index of this entry
    public final Short index() {
        return index;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public PopCount pop() {
        return pop;
    }

    public PopCount clop() {
        return PopCount.of(b.mcount, w.mcount);
    }

    protected BW(short index) {
        super(index);

        this.index = index;
        this.pop = PopCount.get(this.b.count(), this.w.count());
    }

    public String pattern() {
        return pattern(new StringBuilder()).toString();
    }
    
    private static final char[] SIG = {'-', 'X', 'O'};

    public StringBuilder pattern(StringBuilder pattern) {

        int index = this.index;
        int pos = pattern.length();
        for(int i=0; i<8; i++, index/=3) {
            int k = index%3;
            pattern.insert(pos, SIG[k]);

            if(i==3)
                pattern.insert(pos, '|');
        }

        return pattern;
    }

    public static PopCount clop(BW r2, BW r0, BW r1) {
        return PopCount.of(
                Pattern.mcount(r2.b, r0.b, r1.b),
                Pattern.mcount(r2.w, r0.w, r1.w));
    }

    @Override
    public int compareTo(BW o) {
        return Indexed.super.compareTo(o);
    }
}
