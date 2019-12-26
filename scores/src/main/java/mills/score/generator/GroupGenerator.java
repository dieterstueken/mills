package mills.score.generator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.12.19
 * Time: 20:08
 */

import mills.bits.Clops;
import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.function.LongConsumer;

/**
 * Generate a group of score maps.
 * SlicesGroups moved and closed trace back moves from source.
 */
public class GroupGenerator {

    final SlicesGroup<? extends MapSlice> moved;
    final SlicesGroup<? extends ScoreSlice> closed;

    public GroupGenerator(SlicesGroup<? extends MapSlice> moved,
                          SlicesGroup<? extends ScoreSlice> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    IndexProcessor processor(Player player, Score score, boolean closed) {

        boolean swap = player.equals(moved.player());
        Mover mover = Moves.moves(moved.jumps()).mover(swap);
        Score newScore = score.next();
        LongConsumer analyzer = m201 -> propagate(m201, newScore);

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.other());
            int mask = Stones.closed(move);
            if(!closed)
                mask ^= move;
            mover.move(stay, move, mask).normalize().analyze(analyzer);
        };
    }


    void propagate(long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        Slices<? extends MapSlice> slices = moved.group.get(clops);
        int posIndex = slices.scores.index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        mapSlice.propagate(posIndex, i201, newScore.value);
    }
}
