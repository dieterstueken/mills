package mills.score.attic;

import mills.bits.Player;
import mills.position.Position;
import mills.score.ScoreMap;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 18.12.12
 * Time: 19:35
 */
abstract public class Move {

    public static Move forward(ScoreMap from, ScoreMap to) {
        return moves(from, to, false);
    }

    public static Move reverse(ScoreMap from, ScoreMap to) {
        return moves(from, to, true);
    }

    public static Move moves(ScoreMap from, ScoreMap to, boolean reverse) {

        Player player = from.player().other(reverse);
        Moves moves = from.moves(player);

        return new Move(from, to, player, moves, reverse) {
            public Move reverse() {
                boolean reverse = map.player() != player;
                return moves(other, map, reverse);
            }
        };
    }

    public static Move take(ScoreMap self, ScoreMap down) {
        return new Take(self, down);
    }

    /////////////////////////////////////////////////////////////////////

    public interface Analyzer {
        void analyze(long i201);
    }

    public Move analyze(Analyzer analyzer) {
        int size = mover.size();
        for (int i = 0; i < size; ++i)
            analyzer.analyze(mover.get201(i));

        return this;
    }

    final ScoreMap map;
    final ScoreMap other;
    final Player player;
    final Mover mover;

    private Move(ScoreMap map, ScoreMap other, Player player, Moves moves, boolean reverse) {
        this.map = map;
        this.other = other;
        this.i201 = map.i201(0);
        this.player = player;

        reverse ^= other.player() != Player.Black;
        this.mover = moves.mover(reverse);
    }

    public String toString() {
        return String.format("%s %s %s -> %s", this.getClass().getSimpleName(), player, map, other);
    }

    public long get201(int index) {
        return mover.get201(index);
    }

    public int size() {
        return mover.size();
    }

    // debug
    long i201 = 0;

    // debug
    final List<Position> input = new AbstractRandomArray<Position>(1) {

        @Override
        public Position get(int index) {
            return input();
        }
    };

    public Position input() {
        return map.position(i201);
    }

    // debug
    public final List<Position> moved = new AbstractRandomList<Position>() {

        @Override
        public Position get(int index) {
            final long m201 = mover.get201(index);
            return other.position(m201);
        }

        @Override
        public int size() {
            return mover.size();
        }
    };

    abstract public Move reverse();

    public Move close(long i201) {
        return move(i201, true);
    }

    public Move level(long i201) {
        return move(i201, false);
    }

    public Move move(long i201, boolean closed) {
        this.i201 = i201;

        final int stay = Stones.stones(i201, player.other());
        final int move = Stones.stones(i201, player);

        // either closed only or excluding closed
        int mask = Stones.closed(move);
        if(!closed)
            mask ^= move;

        mover.move(stay, move, mask).normalize();

        return this;
    }

    public Move move(long i201) {
        this.i201 = i201;

        final int stay = Stones.stones(i201, player.other());
        final int move = Stones.stones(i201, player);

        // move all
        mover.move(stay, move, move).normalize();

        return this;
    }

    static class Take extends Move {

        Take(ScoreMap map, ScoreMap other, boolean reverse) {
            super(map, other, map.player(), Moves.TAKE, reverse);
        }

        Take(ScoreMap map, ScoreMap other) {
            this(map, other, true);
        }

        public Move reverse() {
            return new Undo(other, map);
        }

        public Move move(long i201) {

            this.i201 = i201;

            final int stay = Stones.stones(i201, player.other());
            final int move = Stones.stones(i201, player);

            // find if opponent closed any mill
            if (!map.moves(player.other()).any(move, stay, Stones.closed(stay))) {
                mover.clear();
                return this;
            }

            // if all moves are closed we may take any of them: mask == move : mask = move
            // else (mask != move) we only take non closed (mask = mask^move)

            int mask = Stones.closed(move);
            if (mask != move)
                mask ^= move;


            mover.move(stay, move, mask).normalize();

            return this;
        }
    }

    static class Undo extends Take {

        Undo(ScoreMap map, ScoreMap other) {
            super(map, other, false);
        }

        public Move reverse() {
            return new Take(other, map);
        }

        public Move move(long i201) {

            this.i201 = i201;

            final int stay = Stones.stones(i201, player.other());
            final int move = Stones.stones(i201, player);

            int free = Stones.free(stay | move);
            int mask = 0;

            // analyze each free position
            for (int m = 1; m < free; m <<= 1) {
                if ((free & m) != 0) {
                    int result = move | m;
                    int closed = Stones.closed(result);
                    if (closed == 0 || closed == result)
                        mask |= m;
                }
            }

            mover.move(stay, move, mask).normalize();

            return this;
        }
    }
}
