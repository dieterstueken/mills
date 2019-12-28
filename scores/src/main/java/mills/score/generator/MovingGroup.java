package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:09
 */
public class MovingGroup<Slice extends ScoreSlice> extends SlicesGroup<Slice> {

    boolean DEBUG = true;

    public boolean closed() {
        return false;
    }

    public MovingGroup(PopCount pop, Player player, List<Slices<Slice>> slices) {
        super(pop, player, slices);
    }

    ScoredPosition position(MovingGroup<? extends ScoreSlice> target, long i201) {
        return target.position(i201);
    }

    void debug(MovingGroup<? extends ScoreSlice> target, long i201) {
        if(DEBUG) {
            Position pos = position(target, i201);
            pos.toString();
        }
    }

    /**
     * Create an IndexProcessor to process Scores of score each slice,
     * The moved positions are then propagated to MovedGroup target.
     * @param target receiving new scores.
     * @param score to analyze.
     * @return an IndexProcessor to process a single slice.
     */
    IndexProcessor processor(MovingGroup<? extends MapSlice> target, Score score) {

        // backtrace moves: move Black
        boolean swap = target.player()!=Player.Black;
        Mover mover = Moves.moves(jumps()).mover(swap);
        Score newScore = score.next();

        return new IndexProcessor() {
            @Override
            public void process(int posIndex, long i201) {
                // reversed move
                int stay = Stones.stones(i201, player);
                int move = Stones.stones(i201, player.other());
                int mask = Stones.closed(move);
                if(!closed())
                    mask ^= move;
                target.debug(MovingGroup.this, i201);
                mover.move(stay, move, mask).normalize().analyze(this::propagate);
            }

            void propagate(long i201) {
                Clops clops = Positions.clops(i201);
                Slices<? extends MapSlice> slices = target.group.get(clops);
                debug(target, i201);
                int posIndex = slices.scores.index.posIndex(i201);
                MapSlice mapSlice = slices.get(posIndex);
                mapSlice.propagate(posIndex, i201, newScore.value);
            }
        };
    }
}
