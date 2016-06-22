package mills.bits;

import mills.util.AbstractRandomArray;

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
 * PopCounts<100 are provided by a pre calculated lookup table.
 */

public class PopCount implements Comparable<PopCount> {

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
    public static int _index(int nb, int nw) {

        // return some negative index, even if it's useless ...
        if (Math.min(nb, nw)<0)
            return -1;

        if(Math.max(nb, nw)<10)
            return 10 * nb + nw;

        // even though this should not happen anyway ...
        // create some pseudo index > 99

        int index = 10 * (nb%10) + (nw%10);
        index += 100 * _index(nb/10, nw/10);

        return index;
    }

    public static int index(int nb, int nw) {

        if (Math.min(nb, nw)<0)
            return -1;

        int m = Math.max(nb, nw)+1;
        m *= m;

        int d = 2*(nb-nw);
        m += d>0 ? 1-d : d;

        return m-1;
    }

    public int index() {
        return 0xff&index;
    }

    public int sum() {
        return nb + nw;
    }

    public boolean isEmpty() {
        return max()==0;
    }

    public PopCount remains() {
        return of(9 - nb, 9 - nw);
    }

    public PopCount truncate(int max) {
        return of(Math.min(nb, max), Math.min(nw, max));
    }

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
    public PopCount mclop() {
        return PopCount.of(mclop(nb), mclop(nw));
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

    /**
     * Subtract an other pop count and return thr remaining population.
     * or null, if the remaining population drops below zero.
     *
     * @param other pop count to subtract
     * @return remaining population or null if negative
     */
    public PopCount sub(final PopCount other) {
        final int nb = this.nb - other.nb;
        final int nw = this.nw - other.nw;
        return of(nb, nw);
    }

    public PopCount add(final PopCount other) {
        final int nb = this.nb + other.nb;
        final int nw = this.nw + other.nw;
        return of(nb, nw);
    }

    public void forEach(Consumer<? super PopCount> action) {
        Objects.requireNonNull(action);
        for(int rb = 0; rb<=nb; ++rb)
            for(int rw = 0; rw<=nw; ++rw) {
                action.accept(of(rb, rw));
            }
    }

    public boolean equals() {
        return nb == nw;
    }

    public boolean valid() {
        return min() >= 3 && max() < 10;
    }

    public PopCount swap() {
        return of(nw, nb);
    }

    public PopCount swapIf(boolean swap) {
        return swap ? swap() : this;
    }

    public int min() {
        return Math.min(nb, nw);
    }

    public PopCount min(PopCount other) {
        return of(Math.min(nb, other.nb), Math.min(nw, other.nw));
    }

    public int max() {
        return Math.max(nb, nw);
    }

    public final Predicate<BW> eq = (bw) -> bw != null && equals(bw.pop);

    public final Predicate<BW> le = (bw) -> bw != null && bw.pop.le(this);

    public final Predicate<BW> gt = (bw) -> bw != null && !bw.pop.le(this);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopCount)) return false;

        PopCount pop = (PopCount) o;

        if (nb != pop.nb) return false;
        if (nw != pop.nw) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public int compareTo(PopCount other) {
        return Integer.compare(index, other.index);
    }

    /**
     * Create a new PopCount.
     * Use factory of() to benefit from pre calculated instances
     *
     * @param nb black population count.
     * @param nw white population count.
     */
    private PopCount(int nb, int nw) {
        this.nb = (byte) nb;
        this.nw = (byte) nw;
        this.index = (byte) index(nb, nw);

        this.string = String.format("%X:%X", nb, nw);
    }

    final String string;

    public String toString() {
        return string;
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

        int index = index(nb, nw);
        if (index < 0)
            return null;

        if (index < SIZE)
            return TABLE.get(index);

        // create an individual PopCount
        return new PopCount(nb, nw);
    }

    ////////////////////////////////////////////////////////////////////////////

    public static final int SIZE = 100;


    // PopCounts <= (9,9)
    public static final List<PopCount> TABLE = AbstractRandomArray.of(table());

    public static final List<PopCount> CLOSED = TABLE.subList(0, 25);

    public static final PopCount EMPTY = of(0,0);
    public static final PopCount P44 = of(4,4);
    public static final PopCount P88 = of(8,8);

    public static PopCount get(int index) {
        return TABLE.get(index);
    }

    private static PopCount[] table() {

        PopCount table[] = new PopCount[SIZE];
        for (int nw = 0; nw < 10; ++nw) {
            for (int nb = 0; nb < 10; ++nb) {
                PopCount p = new PopCount(nb, nw);
                int index = p.index();
                assert table[index]==null;
                table[index] = p;
            }
        }

        return table;
    }

    /**
     * Dump table.
     *
     * @param args unused.
     */
    public static void main(String... args) {

        System.out.println("PopCount index");

        for (int nw = 0; nw < 12; ++nw) {
            for (int nb = 0; nb < 12; ++nb) {
                final PopCount p = PopCount.of(nb, nw);
                System.out.format(" %s %5d", p.toString(), p.index());
            }

            System.out.println();
        }
    }
}
