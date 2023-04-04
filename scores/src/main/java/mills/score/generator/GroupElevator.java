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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.01.20
 * Time: 14:44
 */
public class GroupElevator {

    static final Logger LOGGER = Logger.getLogger(GroupElevator.class.getName());

    final LayerGroup<? extends ScoreSet> moved;

    public GroupElevator(LayerGroup<? extends ScoreSet> moved) {
        this.moved = moved;
    }

    int elevate(ClosingGroup<? extends TargetSlices> closed) {

        LOGGER.log(Level.FINE, ()->String.format(" elevate: %s -> %s(%d)", moved, closed, closed.count()));

        int max = closed.parallelStream()
                .map(TargetSlices::slices)
                .flatMap(Collection::parallelStream)
                .map(Processor::new)
                .mapToInt(Processor::process)
                .reduce(0, Math::max);

        return max;
    }

    class Processor implements IndexProcessor {

        final TargetSlice slice;

        final Mover mover = Moves.TAKE.mover(moved.player==Player.Black);

        Processor(TargetSlice slice) {
            this.slice = slice;
        }

        int process() {
            slice.process(this);
            return slice.max;
        }

        @Override
        public void process(int posIndex, long i201) {
            Player player = slice.player();
            int stay = Stones.stones(i201, player.opponent());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            int mask = move==closed ? closed : move^closed;

            //  Opponent takes a stone with worst result for player.
            mover.move(stay, move, mask); //.normalize();

            int worst = Score.WON.value;
            for(int i=0; i<mover.size(); ++i) {
                long m201 = Positions.normalize(mover.get201(i));
                int score = getScore(m201);

                if(score==0) // nothing worse
                    return;

                if(Score.betterThan(worst, score))
                    worst = score;
            }

            slice.setScore(slice.offset(posIndex), worst);
        }

        int getScore(long m201) {
            PopCount clop = Positions.clop(m201);
            ScoreSet scores = moved.group.get(clop);
            int posIndex = scores.index.posIndex(m201);
            return scores.getScore(posIndex);
        }
    }
}
