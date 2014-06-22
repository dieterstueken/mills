package mills.index2;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 4/18/14
 * Time: 3:35 PM
 */

import mills.bits.PopCount;

import java.util.Objects;

/**
 * Class Mop holds the total population count and the count of closed mills.
 */
public class Mop implements Comparable<Mop> {

    public final PopCount count;
    public final PopCount closed;

    public Mop(PopCount count, PopCount closed) {
        this.count = count;
        this.closed = closed;
    }

    public String toString() {
        return String.format("%s-%s", count, closed);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Mop c = (Mop) o;

        return Objects.equals(count, c.count)
            && Objects.equals(closed, c.closed);
    }

    @Override
    public int hashCode() {
        return count.hashCode() + PopCount.SIZE * closed.hashCode();
    }

    @Override
    public int compareTo(Mop o) {
        int c = PopCount.ORDERING.compare(count, o.count);
        if(c==0)
            c = PopCount.ORDERING.compare(closed, o.closed);

        return c;
    }
}
