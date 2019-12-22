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

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Generate a group of score maps.
 * SlicesGroups moved and closed trace back moves from source.
 */
public class GroupGenerator {

    final SlicesGroup<? extends ScoreSlice> moved;
    final SlicesGroup<? extends ScoreSlice> closed;
    final SlicesGroup<? extends MapSlice> source;

    public GroupGenerator(SlicesGroup<MapSlice> moved, SlicesGroup<MapSlice> closed, SlicesGroup<MapSlice> source) {
        this.moved = moved;
        this.closed = closed;
        this.source = source;
    }

    public int process(Score score) {

        List<ForkJoinTask<?>> tasks = Stream.concat(
                moved.slices(score).map(move(score)),
                closed.slices(score).map(close(score))
        ).collect(Collectors.toList());

        if(!tasks.isEmpty())
            ForkJoinTask.invokeAll(tasks);

        return tasks.size();
    }

    Function<ScoreSlice, RecursiveAction> move(Score score) {
        return slice -> task(slice, score, false);
    }

    Function<ScoreSlice, RecursiveAction> close(Score score) {
        return slice -> task(slice, score, true);
    }

    RecursiveAction task(ScoreSlice slice, Score score, boolean closed) {

        Player player = slice.player();
        boolean swap = slice.player().equals(source.player());
        Mover mover = Moves.moves(source.jumps()).mover(swap);
        Score newScore = score.next();
        LongConsumer analyzer = m201 -> propagate(m201, newScore);

        IndexProcessor processor = (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.other());
            int mask = Stones.closed(move);
            if(!closed)
                mask ^= move;
            mover.move(stay, move, mask).normalize().analyze(analyzer);
        };

        return new RecursiveAction() {

            @Override
            protected void compute() {
                slice.processScores(processor, score);
            }
        };
    }

    void propagate(long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        Slices<? extends MapSlice> slices = source.group.get(clops);
        int posIndex = slices.scores.index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        mapSlice.propagate(posIndex, i201, newScore.value);
    }
}
