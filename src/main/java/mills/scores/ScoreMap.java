package mills.scores;

import com.google.common.base.Function;
import com.google.common.collect.Ordering;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Position;
import mills.position.Situation;
import mills.stones.MoveTable;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.util.HashSet;
import java.util.Set;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  16.11.12 17:24
 * modified by: $Author$
 * modified on: $Date$
 */
public class ScoreMap implements Position.Factory, Closeable {

    private final ByteBuffer scores;

    private final PosIndex index;

    final Situation situation;

    public ScoreMap(final ByteBuffer scores, final PosIndex index, final Situation situation) {
        this.scores = scores;
        this.index = index;
        this.situation = situation;

        assert situation.pop.equals(index.pop());
        assert scores.limit() >= index.size();
    }

    public String toString() {
        return situation.toString();
    }

    public Player player() {
        return situation.player;
    }

    public void force() {
        if(scores instanceof MappedByteBuffer) {
            ((MappedByteBuffer) scores).force();
        }
    }

    public void close() throws IOException {}

    /**
     * Get current score as an unsigned value.
     *
     * @param index to seek.
     * @return current score.
     */
    public int getScore(int index) {
        int value = scores.get(index);
        value &= 0xff;  // clip off sign bit

        return value;
    }

    public void setScore(int posIndex, int score) {
        byte value = (byte) (score&0xff);
        scores.put(posIndex, value);
    }

    public PosIndex index() {
        return index;
    }

    public int size() {
        return index.size();
    }

    public long i201(int posIndex) {
        return index.i201(posIndex);
    }

    public void process(IndexProcessor processor, int base, int i) {
        index.process(processor, base, i);
    }

    public PopCount pop() {
        return index.pop();
    }

    public int posIndex(long i201) {
        return index.posIndex(i201);
    }

    public MoveTable moves(Player player) {
        boolean jumps = pop().jumps(player);
        return MoveTable.moves(jumps);
    }

    public static final Function<Position, Integer> POS_INDEX = new Function<Position, Integer>() {
        @Nullable
        @Override
        public Integer apply(@Nullable Position position) {
            return position==null ? null : position.posIndex;
        }
    };

    public static final Ordering<Position> INDEX_ORDER = Ordering.<Integer>natural().onResultOf(POS_INDEX);

    public Situation situation() {
        return situation;
    }

    public class Position extends mills.position.Position {

        final Position normalized;

        final int posIndex;
        final int score;

        public Position(long i201) {
            super(i201);

            posIndex = index.posIndex(i201);
            score = posIndex<0 ? -1 : getScore(posIndex);

            if(super.normalized)
                normalized = this;
            else
                normalized = position(i201(posIndex));
        }

        public Position position(long i201) {
            return new Position(i201);
        }

        @Override
        public StringBuilder format(StringBuilder sb) {
            sb = super.format(sb);
            sb.insert(3, player().name().charAt(0));
            sb.append(" ").append(posIndex).append(" : ");
            sb.append(score);
            return sb;
        }
    }

    public Position position(long i201) {
        return new Position(i201);
    }

    public Position indexPosition(int posIndex) {
        long i201 = i201(posIndex);
        return new Position(i201);
    }

    public final Set<Position> debug = new HashSet<Position>();

    public void debug(Position position) {
        debug.add(position);
    }

    public void debug(int index) {
        debug.add(position(i201(index)));
    }

    public void debug(long i201) {
        debug.add(position(i201));
    }

    public Thread load() {
        Thread thread = new Thread() {
            public void run() {
                for (int i = 0; i < scores.limit(); i += 4096)
                    scores.get(i);
            }

        };

        thread.start();

        Thread.yield();

        return thread;
    }
}
