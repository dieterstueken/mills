package mills.position;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.09.12
 * Time: 13:36
 */

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.RingEntry;
import mills.stones.Stones;
import mills.util.AbstractRandomArray;

import java.util.List;

import static mills.position.Positions.*;

/**
 * Class Position represents unfolded information about a position.
 * It's primary use are debug information.
 */
public class Position implements Comparable<Position> {

    public interface Factory {
        Position position(long i201);
    }

    // ordering: i1:i0:i2

    public final long i201;

    public final PopCount pop;
    public final boolean normalized;
    public final int perm;
    public final RingEntry r2;
    public final RingEntry r0;
    public final RingEntry r1;

    public final int black;
    public final int white;

    public final List<Position> permuted = new AbstractRandomArray<Position>(16) {

        @Override
        public Position get(int index) {
            return Position.this.permute(index);
        }
    };

    public final List<Position> swapped = new AbstractRandomArray<Position>(1) {
        @Override
        public Position get(int index) {
            return swap();
        }
    };

    private Position permute(int perm) {
        if(perm==0)
            return this;

        long p201 = Positions.permute(i201, perm);
        return position(p201);
    }

    public Position position(long i201) {
        return new Position(i201);
    }

    public Position swap() {
        long x201 = Positions.swapped(i201);
        return new Position(x201);
    }

    public Position(long i201) {

        this.i201 = i201;

        pop = pop(i201);
        normalized = normalized(i201);
        perm = perm(i201);

        r2 = r2(i201);
        r0 = r0(i201);
        r1 = r1(i201);

        black = Stones.stones(i201, Player.Black);
        white = Stones.stones(i201, Player.White);
    }

    public static String format(RingEntry e) {
        return String.format(" %4d [%s]", e.index(), e.pattern());
    }

    public StringBuilder format(StringBuilder sb) {
        sb.append(pop.nb);
        sb.append(normalized ? "!": ":");
        sb.append(pop.nw);

        // reverse order
        sb.append(format(r1));
        sb.append(format(r0));
        sb.append(format(r2));

        return sb;
    }

    public String toString() {
        return format(new StringBuilder()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position)) return false;

        Position position = (Position) o;

        return i201 == position.i201;
    }

    @Override
    public int hashCode() {
        return (int) (i201 ^ (i201 >>> 32));
    }

    @Override
    public int compareTo(Position o) {
        return ComparisonChain.start()
                .compare(this.r1, o.r1)
                .compare(this.r0, o.r0)
                .compare(this.r2, o.r2)
                .result();
    }

    public static final Ordering<Position> INDEX_ORDER = Ordering.natural();
}
