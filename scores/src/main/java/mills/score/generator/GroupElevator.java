package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.Collection;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.01.20
 * Time: 14:44
 */
public class GroupElevator {

    static final Logger LOGGER = Logger.getLogger(GroupElevator.class.getName());

    final LayerGroup<? extends ScoreSet> moved;

    final ClosingGroup<? extends MapSlices> closed;

    public GroupElevator(LayerGroup<? extends ScoreSet> moved, ClosingGroup<? extends MapSlices> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    ClosingGroup<? extends MapSlices> generate() {

        LOGGER.log(Level.INFO, ()->String.format(" elevate: %s -> %s(%d)", moved, closed, closed.count()));

        ForkJoinTask.invokeAll(closed.stream()
                .map(MapSlices::slices)
                .flatMap(Collection::stream)
                .map(slice->new RecursiveAction() {
                    @Override
                    protected void compute() {
                        slice.process(new Processor(slice));
                    }
                }).collect(Collectors.toList()));

        return closed;
    }

    class Processor implements IndexProcessor {

        final MapSlice slice;

        final Mover mover = Moves.TAKE.mover(moved.player==Player.Black);

        Processor(MapSlice slice) {
            this.slice = slice;
        }

        @Override
        public void process(int posIndex, long i201) {
            Player player = slice.player();
            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            int mask = move==closed ? closed : move^closed;

            mover.move(stay, move, mask).normalize();

            //  Opponent takes a stone with worst result for player.

            int worsest = Score.WON.value;
            for(int i=0; i<mover.size(); ++i) {
                long m201 = mover.get201(i);
                int score = getScore(m201);
                if(Score.betterThan(worsest, score))
                    worsest = score;
            }

            slice.setScore(slice.offset(posIndex), worsest);
        }

        int getScore(long m201) {
            PopCount clop = Positions.clop(m201);
            ScoreSet scores = moved.group.get(clop);
            int posIndex = scores.index.posIndex(m201);
            return scores.getScore(posIndex);
        }
    }
}
