package mills.bits;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

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

public class PopCount {

    public final byte nb;
    public final byte nw;
    public final byte index;

    public byte nb() {
        return nb;
    }

    public byte nw() {
        return nw;
    }

    /**
     * Create some index function for a pair of population counts.
     *
     * @param nb black population count.
     * @param nw white population count.
     * @return a compact index for positive populations.
     */
    public static int index(int nb, int nw) {

        // return some negative index, even if it's useless ...
        if (Math.min(nb, nw)<0)
            return -1;

        if(Math.max(nb, nw)<10)
            return 10 * nb + nw;

        // even though this should not happen anyway ...
        // create some pseudo index > 99

        int index = 10 * (nb%10) + (nw%10);
        index += 100 * index(nb/10, nw/10);

        return index;
    }

    public byte index() {
        return index;
    }


    public int sum() {
        return nb + nw;
    }

    public int count(Player player) {

        switch(player) {
            case Black: return nb();
            case White: return nw();
        }

        return 0;
    }

    public boolean isEmpty() {
        return max()==0;
    }

    public boolean jumps(final Player who) {
        return who.count(this) <= 3;
    }

    public PopCount remains() {
        return of(9 - nb, 9 - nw);
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
        final int nb = this.nb() - other.nb;
        final int nw = this.nw() - other.nw;
        return of(nb, nw);
    }

    public PopCount sub(final Player who) {
        return sub(who.pop);
    }

    public PopCount add(final PopCount other) {
        final int nb = this.nb() + other.nb;
        final int nw = this.nw() + other.nw;
        return of(nb, nw);
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

    public int max() {
        return Math.max(nb, nw);
    }

    public final Predicate<BW> eq = (bw) -> bw != null && equals(bw.pop);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PopCount)) return false;

        PopCount pop = (PopCount) o;

        if (nb != pop.nb) return false;
        if (nw != pop.nw) return false;

        return true;
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
        this.string = String.format("%d:%d", nb, nw);
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

    public static Comparator<PopCount> ORDERING = Comparator.nullsFirst(Comparator.comparingInt(PopCount::index));

    public static final int SIZE = 100;

    // PopCounts <= (9,9)
    public static final ImmutableList<PopCount> TABLE = createTable();

    public static Predicate<PopCount> VALID = pop -> pop != null && pop.valid();

    public static final Function<PopCount, Integer> MAX = pop -> pop==null ? null : pop.max();

    public static PopCount get(int index) {
        return TABLE.get(index);
    }

    //public static final Array<PopCount> ARRAY = Array.array(Iterables.toArray(TABLE, PopCount.class));

    private static ImmutableList<PopCount> createTable() {

        // make an empty list preset with all entries == null
        final List<PopCount> table = Arrays.asList(new PopCount[SIZE]);

        for (int nw = 0; nw < 10; ++nw) {
            for (int nb = 0; nb < 10; ++nb) {
                final PopCount p = new PopCount(nb, nw);
                table.set(p.index(), p);
            }
        }

        // will complain by a NullPointerException if any element remained empty.
        return ImmutableList.copyOf(table);
    }

    /**
     * Dump table.
     *
     * @param args unused.
     */
    public static void main(String... args) {
        System.out.println("PopCount table");

        for (int nw = -10; nw < 10; ++nw) {
            for (int nb = -10; nb < 10; ++nb) {
                //final PopCount p = PopCount.of(nb, nw);
                //System.out.format("%3d", p.index());
                System.out.format("%5d", index(nb, nw));
            }

            System.out.println();
        }
    }
}
