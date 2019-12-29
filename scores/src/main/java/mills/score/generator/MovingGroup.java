package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.function.LongConsumer;

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

    void debug(MovedGroup target, long i201) {
        if(DEBUG) {
            Position pos = position(target, i201);
            pos.toString();
        }
    }

    /**
     * Create an IndexProcessor to process Scores of score each slice,
     * The moved positions are then propagated to MovedGroup target.
     * @param target receiving new scores.
     * @param analyzer to analyze moved positions.
     * @return an IndexProcessor to process a single slice.
     */
    IndexProcessor processor(MovedGroup target, LongConsumer analyzer) {

        // backtrace moves: move Black
        boolean swap = target.player()!=Player.Black;
        Mover mover = Moves.moves(jumps()).mover(swap);

        return new IndexProcessor() {
            @Override
            public void process(int posIndex, long i201) {
                // reversed move
                int stay = Stones.stones(i201, player);
                int move = Stones.stones(i201, player.other());
                int mask = Stones.closed(move);
                if (!closed())
                    mask ^= move;
                debug(target, i201);
                mover.move(stay, move, mask).normalize().analyze(analyzer);
            }
        };
    }

    public ScoredPosition position(MovedGroup target, long i201, Player player) {
        if(player == this.player)
            return position(target, i201);

        i201 = Positions.inverted(i201);
        return position(target, i201).inverted;
    }

    public ScoredPosition position(MovedGroup target, long i201) {
        return position(i201);
    }
}
