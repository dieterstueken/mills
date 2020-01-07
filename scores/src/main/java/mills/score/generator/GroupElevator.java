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

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.01.20
 * Time: 14:44
 */
public class GroupElevator {

    final LayerGroup<? extends ScoreSet> moved;

    final ClosingGroup<? extends MapSlices> closed;

    public GroupElevator(LayerGroup<? extends ScoreSet> moved, ClosingGroup<? extends MapSlices> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    ClosingGroup<? extends MapSlices> generate() {
        closed.stream().map(MapSlices::slices).flatMap(Collection::stream).parallel().forEach(this::process);
        return closed;
    }

    void process(MapSlice slice) {
        slice.process(new Processor(slice));
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

            int best = Score.LOST.value;
            for(int i=0; i<mover.size(); ++i) {
                long m201 = mover.get201(i);
                int score = getScore(m201);
                if(Score.betterThan(score, best))
                    best = score;
            }

            slice.setScore(slice.offset(posIndex), best);
        }

        int getScore(long m201) {
            PopCount clop = Positions.clop(m201);
            ScoreSet scores = moved.group.get(clop);
            int posIndex = scores.index.posIndex(m201);
            return scores.getScore(posIndex);
        }
    }
}
