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
import mills.position.Position;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

/**
 * Generate a group of score maps.
 * SlicesGroups moved and closed trace back moves from source.
 */
public class GroupGenerator {
    boolean DEBUG = true;

    final SlicesGroup<? extends MapSlice> moved;
    final SlicesGroup<? extends ScoreSlice> closed;

    public GroupGenerator(SlicesGroup<? extends MapSlice> moved,
                          SlicesGroup<? extends ScoreSlice> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    IndexProcessor processor(ScoreSlice slice, Score score, boolean closed) {
        Player player = slice.player();
        boolean swap = moved.player()==Player.White;
        Mover mover = Moves.moves(moved.jumps()).mover(swap);
        Score newScore = score.next();

        return new IndexProcessor() {

            void debug(long i201) {
                if(DEBUG) {
                    Position pos = slice.position(i201);
                    pos.toString();
                }
            }

            @Override
            public void process(int posIndex, long i201) {
                // reversed move
                int stay = Stones.stones(i201, player);
                int move = Stones.stones(i201, player.other());
                int mask = Stones.closed(move);
                if(!closed)
                    mask ^= move;
                debug(i201);
                mover.move(stay, move, mask).normalize().analyze(this::propagate);
            }

            void propagate(long i201) {
                Clops clops = Positions.clops(i201);
                Slices<? extends MapSlice> slices = moved.group.get(clops);
                debug(i201);
                int posIndex = slices.scores.index.posIndex(i201);
                MapSlice mapSlice = slices.get(posIndex);
                mapSlice.propagate(posIndex, i201, newScore.value);
            }
        };
    }
}
