package mills.position;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 15.09.12
 * Time: 13:36
 */

import mills.bits.Perm;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.ring.RingEntry;
import mills.stones.Stones;
import mills.util.AbstractRandomList;

import java.util.List;

import static mills.position.Positions.i201;
import static mills.position.Positions.normalized;
import static mills.position.Positions.perms;
import static mills.position.Positions.pop;
import static mills.position.Positions.r0;
import static mills.position.Positions.r1;
import static mills.position.Positions.r2;

/**
 * Class Position represents unfolded information about a position.
 * It's primary use are debug information.
 */
public class Position implements Comparable<Position> {

    public interface Factory {
        Position position(long i201);
        default Position position(int i2, int i0, int i1) {
            return position(i201((short)i2, (short)i0, (short)i1));
        }
    }

    protected final Player player;

    public static Position of(long i201) {
        return new Position(i201);
    }
    public static Position of(int black, int white) {
        return new Position(Stones.i201(black, white));
    }

    // the NORMALIZED empty position.
    public static final Position EMPTY = of(Positions.NORMALIZED);

    // ordering: i1:i0:i2

    public final long i201;

    public final PopCount pop;
    public final PopCount clop;
    public final boolean isNormalized;
    public final int perm;
    public final RingEntry r2;
    public final RingEntry r0;
    public final RingEntry r1;

    public final int black;
    public final int white;

    public final String board;

    public final List<Position> permuted = AbstractRandomList.virtual(16, Position.this::permute);

    public final Position inverted;

    public Position permute(int perm) {
        if(perm==0)
            return this;

        long p201 = Positions.permute(i201, perm);
        return position(p201);
    }

    public Position permute(Perm perm) {
        return permute(perm.ordinal());
    }

    protected Position inverted() {
        return new Position(i201, player.opponent()) {
            public Position inverted() {
                return Position.this;
            }
        };
    }

    protected Position position(long i201) {
        return of(i201);
    }

    public Position(long i201) {
        this(i201, Player.White);
    }
    public Position(long i201, Player player) {

        this.i201 = i201;
        this.player = player;
        this.board = Board.board(i201, player);

        pop = pop(i201);
        clop = Positions.clop(i201);
        isNormalized = normalized(i201);
        perm = perms(i201);

        r2 = r2(i201);
        r0 = r0(i201);
        r1 = r1(i201);

        black = Stones.stones(i201, Player.Black);
        white = Stones.stones(i201, Player.White);

        inverted = inverted();
    }

    public long m201() {
        return Positions.m201(i201);
    }

    public static String format(RingEntry e) {
        return String.format(" %4d [%s]", e.index(), e.pattern());
    }

    public StringBuilder format(StringBuilder sb) {
        sb.append(pop.nb);
        sb.append(isNormalized ? "!": ":");
        sb.append(pop.nw);
        sb.append('[').append(clop).append(']');

        sb.append(format(r2));
        sb.append(format(r0));
        sb.append(format(r1));

        return sb;
    }

    public String toString() {
        return format(new StringBuilder()).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Position position)) return false;

        return i201 == position.i201;
    }

    @Override
    public int hashCode() {
        return (int) (i201 ^ (i201 >>> 32));
    }

    @Override
    public int compareTo(Position o) {
        int result = r1.compareTo(o.r1);
        if(result!=0)
            return result;

        result = r0.compareTo(o.r0);
        if(result!=0)
            return result;

        result = r2.compareTo(o.r2);
        return result;
    }
}
