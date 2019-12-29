package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

    public boolean propagate(MovedGroup target, Score score) {

        List<? extends ScoreSlice> slices = Stream.concat(group.values().stream(), closed.group.values().stream())
                .flatMap(Slices::stream)
                .filter(slice->slice.hasScores(score))
                .collect(Collectors.toList());

        if(slices.isEmpty())
            return false;

        slices.parallelStream()
                .filter(slice -> slice.any(score))
                .forEach(slice -> {
                    LongConsumer analyzer = m201 -> target.propagate(this, m201, score.next());
                    IndexProcessor processor = processor(target, analyzer);
                    slice.processScores(processor, score);
                });

        return true;
    }

    void propagate(MovedGroup source, long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        Slices<? extends MapSlice> slices = group.get(clops);
        debug(source, i201);
        int posIndex = slices.scores.index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        mapSlice.propagate(posIndex, i201, newScore.value);
    }

    public class MovedPosition extends ScoredPosition {

        final MovedGroup target;

        final List<ScoredPosition> moved = new ArrayList<>();

        @Override
        protected MovedPosition position(long i201, Player player, int score, ScoredPosition inverted) {
            return new MovedPosition(target, i201, player, score, inverted);
        }

        public MovedPosition(MovedGroup target, long i201, Player player, int score, ScoredPosition inverted) {
            super(i201, player, score, inverted);
            this.target = target;

            Moves moves = Moves.moves(jumps());
            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            moves.move(stay, move, move, (s, m, mask) -> {
                int moved = move ^ mask;
                long m201 = player==Player.White ? Stones.i201(stay, moved) : Stones.i201(moved, stay);
                boolean closing = (Stones.closed(moved) & ~closed) != 0;
                ScoredPosition position = movedPosition(target, m201, closing);
                this.moved.add(position);
                return !Moves.ABORT;
            });
        }
    }

    public ScoredPosition movedPosition(MovedGroup target, long i201, boolean closing) {
        if(closing)
            return closed.position(i201, player.other());
        else
            return this.position(target, i201, player.other());
    }

    public MovedPosition position(MovedGroup target, long i201) {
        int score = getScore(i201);
        return new MovedPosition(target, i201, player, score, null);
    }
}
