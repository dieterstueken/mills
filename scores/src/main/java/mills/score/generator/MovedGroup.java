package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 21:18
 */
public class MovedGroup extends MovingGroup<MapSlice> {
    
    final ClosedGroup closed;
    
    public MovedGroup(PopCount pop, Player player, ClosedGroup closed, List<Slices<MapSlice>> slices) {
        super(pop, player, slices);
        this.closed = closed;
    }
    
    MovedPosition position(MovedGroup target, long i201) {
        int score = getScore(i201);
        return new MovedPosition(target, i201, player, score, null);
    }

    public class MovedPosition extends ScoredPosition {

        final MovedGroup target;
        final List<MovedPosition> moved;
        final List<ScoredPosition> closed;

        @Override
        protected MovedPosition position(long i201, Player player, int score, ScoredPosition inverted) {
            return new MovedPosition(target, i201, player, score, inverted);
        }

        public MovedPosition(MovedGroup target, long i201, Player player, int score, ScoredPosition inverted) {
            super(i201, player, score, inverted);
            this.target = target;
            moved = moved();
            closed = closed();
        }

        List<MovedPosition> moved() {
            Mover mover = Moves.moves(jumps()).mover(target.player!=Player.White);
            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            mover.move(stay, move, move^closed);
            return mover.transform(m201->target.position(MovedGroup.this, m201));
        }

        List<ScoredPosition> closed() {
            Mover mover = Moves.moves(jumps()).mover(target.closed.player!=Player.White);
            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            mover.move(stay, move, closed);
            return mover.transform(m201->target.closed.position(MovedGroup.this, m201));
        }
    }
}
