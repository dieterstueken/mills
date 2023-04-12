package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Position;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.01.20
 * Time: 14:44
 */
public class GroupElevator extends LayerGroup<ScoreSet> {

    static final Logger LOGGER = Logger.getLogger(GroupElevator.class.getName());

    private GroupElevator(PopCount pop, Player player, Map<PopCount, ? extends ScoreSet> group) {
        super(pop, player, group);
    }

    static GroupElevator create(PopCount pop, Player player, Map<PopCount, ? extends ScoreSet> group) {
        return new GroupElevator(pop, player, group);
    }

    static GroupElevator create(LayerGroup<? extends ScoreSet> group) {
        return create(group.pop, group.player, group.group);
    }

    ScoreSlices elevate(ScoreTarget scores) {

        TargetSlices slices = TargetSlices.of(scores);

        int max = slices.slices().parallelStream()
                .map(Processor::new)
                .mapToInt(Processor::process)
                .reduce(0, Math::max);

        LOGGER.log(Level.FINE, ()->String.format("elevate: %s <- %s M:%d", scores, this, max));

        return slices;
    }

    class Processor extends Mover implements IndexProcessor {

        final TargetSlice slice;

        transient long i201;

        final List<ScoredPosition> debug = new AbstractRandomList<>() {

            @Override
            public int size() {
                return Processor.this.size();
            }

            @Override
            public ScoredPosition get(int index) {
                long i201 = get201(index);
                int score = getScore(i201);
                return new ScoredPosition(i201, player(), score);
            }
        };

        Position position() {
            return new Position(i201, slice.player());
        }

        Processor(TargetSlice slice) {
            super(Moves.TAKE, player()==Player.Black);

            this.slice = slice;
        }

        int process() {
            slice.process(this);
            return slice.max;
        }

        @Override
        public void process(int posIndex, long i201) {
            this.i201 = i201;

            Player player = slice.player();
            // take an opponents stone
            int move = Stones.stones(i201, player);
            int stay = Stones.stones(i201, player.opponent());
            int closed = Stones.closed(move);
            int mask = move==closed ? closed : move^closed;

            int size = move(stay, move, mask).size();

            if(size==0)
                throw new IllegalStateException("no moves");

            int worst = Score.WON.value;

            for(int i=0; i<size; ++i) {
                long m201 = Positions.normalize(get201(i));
                int score = getScore(m201);
                if(Score.betterThan(worst, score))
                    worst = score;
            }

            if(worst!=0)
                slice.setScore(slice.offset(posIndex), worst);
        }

        int getScore(long m201) {
            assert Positions.pop(m201).equals(pop) : "swapped";

            PopCount clop = Positions.clop(m201);
            ScoreSet scores = group.get(clop);
            int posIndex = scores.index.posIndex(m201);
            return scores.getScore(posIndex);
        }
    }
}
