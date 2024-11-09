package mills.bits;

import mills.util.Indexed;
import mills.util.Indexer;
import mills.util.listset.DirectListSet;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.09.2010
 * Time: 15:12:08
 */

/**
 * Class PopCount defines the population of black and white stones.
 * PopCounts are always positive.
 * PopCounts<100 are provided by a precalculated lookup table.
 */

public class PopCount implements Indexed, Comparable<PopCount> {

    public final byte nb;
    public final byte nw;
    public final byte index;

    public final byte nb() {
        return nb;
    }

    public final byte nw() {
        return nw;
    }

    /**
     * Create some index function for a pair of population counts.
     *
     * @param nb black population count.
     * @param nw white population count.
     * @return a compact index for positive populations.
     */
    public static int getIndex(int nb, int nw) {
        PopCount pop = of(nb, nw);
        if(pop==null)
            throw new IllegalArgumentException("invalid pop count");
        return pop.index;
    }

    public int getIndex() {
        return 0xff&index;
    }

    @Override
    public int compareTo(PopCount o) {
        return Indexed.super.compareTo(o);
    }

    public int sum() {
        return nb + nw;
    }

    public int div() {
        return Math.abs(nb -nw);
    }

    public boolean isEven() {
        return sum()%2==0;
    }

    // fits into a single ring
    public boolean singleRing() {
        return sum()<=8;
    }

    public boolean isEmpty() {
        return max()==0;
    }

    public boolean isSym() {
        return nb==nw;
    }

    public PopCount remains() {
        return _of(9 - nb, 9 - nw);
    }

    /**
     * How many mills are possible with a given count of stones.
     * @param count of stones
     * @return max # of closed mills possible
     */
    public static int mclop(int count) {
        if(count>=8)
            return 4;
        if(count>=7)
            return 3;
        if(count>=5)
            return 2;
        return count>=3 ? 1 : 0;
    }

    // maximum possible closed population

    /**
     * Maximum possible closed population.
     * Since each closed mill takes an opponent stone
     * the closed count + opponents count is limited to 9.
     * @param limited if the # closed mills is limited by the opponents count.
     * @return the maximum count of closed mills possible.
     */
    public PopCount mclop(boolean limited) {
        // limit by given stones
        PopCount mclop = PopCount.get(mclop(nb), mclop(nw));

        if(limited) {
            // limit by missing stones.
            PopCount limit = PopCount.P99.sub(this).swap();
            mclop = mclop.min(limit);
        }

        return mclop;
    }

    public PopCount mclop() {
        return mclop(false);
    }

    /**
     * Return if this PopCount has less or equal occupations than another one.
     * An instance of PopCount is never le than null.
     *
     * @param other PopCount to compare to (may be null)
     * @return if this <= other.
     */

    public boolean le(PopCount other) {
        return other != null && nb <= other.nb && nw <= other.nw;
    }

    public boolean lt(PopCount other) {
        return other != null && nb < other.nb || nw < other.nw;
    }

    public boolean ge(PopCount other) {
        return other != null && nb >= other.nb && nw >= other.nw;
    }

    /**
     * Subtract another pop count and return thr remaining population.
     * or null, if the remaining population drops below zero.
     *
     * @param other pop count to subtract
     * @return remaining population or null if negative
     */
    public PopCount sub(final PopCount other) {
        final int nb = this.nb - other.nb;
        final int nw = this.nw - other.nw;
        return _of(nb, nw);
    }

    public PopCount add(final PopCount other) {
        final int nb = this.nb + other.nb;
        final int nw = this.nw + other.nw;
        return _of(nb, nw);
    }

    public PopCount max(final PopCount other) {
        final int nb = Math.max(this.nb, other.nb);
        final int nw = Math.max(this.nw, other.nw);
        return _of(nb, nw);
    }

    public PopCount min(PopCount other) {
        final int nb = Math.min(this.nb, other.nb);
        final int nw = Math.min(this.nw, other.nw);
        return _of(nb, nw);
    }

    public void forEach(Consumer<? super PopCount> action) {
        Objects.requireNonNull(action);
        for(int rb = 0; rb<=nb; ++rb)
            for(int rw = 0; rw<=nw; ++rw) {
                action.accept(_of(rb, rw));
            }
    }

    public boolean equals() {
        return nb == nw;
    }

    public boolean valid() {
        return min() >= 3 && max() < 10;
    }

    public PopCount swap() {
        return _of(nw, nb);
    }

    public PopCount swapIf(boolean swap) {
        return swap ? swap() : this;
    }

    public int min() {
        return Math.min(nb, nw);
    }

    public int max() {
        return Math.max(nb, nw);
    }

    public final Predicate<BW> eq = (bw) -> bw != null && equals(bw.pop);

    public final Predicate<BW> le = (bw) -> bw != null && bw.pop.le(this);

    @Override
    public boolean equals(Object o) {
        return o==this || o instanceof PopCount pop && equals(pop);
    }

    public boolean equals(PopCount pop) {
           if (nb != pop.nb) return false;
           if (nw != pop.nw) return false;
           return true;
       }

    @Override
    public int hashCode() {
        return index;
    }

    private final String string;

    public String toString() {
        return string;
    }

    private PopCount _of(int nb, int nw) {
        if(nb==this.nb && nw==this.nw)
            return this;

        return of(nb, nw);
    }

    /**
     * Return a PopCount, either from the predefined table or create some new one.
     * Return null if either count is negative.
     *
     * @param nb black population count.
     * @param nw white population count.
     * @return a PopCount describing the given occupations, or null if negative.
     */
    public static PopCount of(int nb, int nw) {

        if (Math.min(nb, nw)<0)
            return null;
        else
            return get(nb, nw);
    }

    /**
     * Get a PopCount from given positive population counts.
     * PopCount beyond 100 are  not cached but generated.
     *
     * @param nb black population count.
     * @param nw white population count.
     * @return a PopCount describing the given occupations.
     */
    public static PopCount get(int nb, int nw) {
        if(Math.min(nb, nw)<0)
            return null;

        if(Math.max(nb, nw)>9)
            return null;

        return VALUES.get(10*nb + nw);
    }

    ////////////////////////////////////////////////////////////////////////////

    /**
     * Create a new PopCount.
     * Use factory of() to benefit from pre calculated instances
     *
     * @param nb black population count.
     * @param nw white population count.
     */
    private PopCount(int nb, int nw, int index) {
        this.nb = (byte) nb;
        this.nw = (byte) nw;
        this.index = (byte) index;

        this.string = String.format("%X:%X", nb, nw);
    }

    public static final int SIZE = 100;

    // internal list by 10*nb+nw.
    public static final List<PopCount> VALUES;

    // ordered by index.
    public static final DirectListSet<PopCount> TABLE;

    public static PopCount get(int index) {
        return TABLE.get(index);
    }

    static {

        // indexes to create for 10*nb+nw.
        int[] index = {
                     0, 1, 5, 9,13,25,29,35,43,53,
                     2, 3, 7,11,17,27,33,41,51,63,
                     4, 6,10,15,21,31,39,49,61,71,
                     8,12,16,19,23,37,47,59,69,77,
                    14,18,20,22,24,45,57,67,75,83,
                    26,28,32,38,46,55,65,73,81,89,
                    30,34,40,48,56,64,72,79,87,93,
                    36,42,50,58,66,74,80,85,91,95,
                    44,52,60,68,76,82,86,90,94,97,
                    54,62,70,78,84,88,92,96,98,99
            };

        // create values.
        PopCount[] values = new PopCount[100];

        Arrays.setAll(values, bw -> new PopCount(bw/10, bw%10, index[bw]));
        VALUES = List.of(values);

        Arrays.sort(values, Indexer.INDEXED);
        TABLE = DirectListSet.of(values);
    }

    public static final PopCount EMPTY = get(0,0);
    public static final PopCount P11 = get(1,1);
    public static final PopCount P33 = get(3,3);
    public static final PopCount P44 = get(4,4);
    public static final PopCount P88 = get(8,8);
    public static final PopCount P99 = get(9,9);

    // # of entries with pop.sum() < 9 (single ring)
    public static final int SRPOPS = 9*10/2;
    public static final DirectListSet<PopCount> SRPOP = TABLE.headSet(SRPOPS);

    // # of closed mills (0-4)
    public static final int NCLOPS = P44.index+1;
    public static final DirectListSet<PopCount> CLOPS = TABLE.headSet(NCLOPS);

    /**
     * Dump table.
     *
     * @param args unused.
     */
    public static void main(String... args) {

        System.out.println("PopCount index");

        System.out.print("b\\w");
        for (int nw = 0; nw < 10; ++nw) {
            System.out.format("  %X ", nw);
        }
        System.out.println();

        for (int nb = 0; nb < 10; ++nb) {
            System.out.format("%X ", nb);

            for (int nw = 0; nw < 10; ++nw) {
                final PopCount p = PopCount.of(nb, nw);
                System.out.format("%4d", p.getIndex());
            }

            System.out.println();
        }
    }
}
