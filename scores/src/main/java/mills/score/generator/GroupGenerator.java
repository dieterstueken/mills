package mills.score.generator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.12.19
 * Time: 20:08
 */

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static mills.score.Score.Result.LOST;

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
        )
                .map(ForkJoinTask::adapt)
                .collect(Collectors.toList());

        if(!tasks.isEmpty())
            ForkJoinTask.invokeAll(tasks);

        return tasks.size();
    }

    Function<ScoreSlice, Runnable> move(Score score) {

        if (score.is(LOST)) {
            return slice -> () -> slice.processScores(moveLost(slice, score), score);
        } else
            return slice -> () -> {};
    }

    Function<ScoreSlice, Runnable> close(Score score) {

        return slice -> () -> {};
    }

    IndexProcessor moveLost(ScoreSlice slice, Score score) {

        Mover mover = Moves.moves(source.jumps()).mover(slice.player().equals(source.player()));
        Player player = slice.player();

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.other());

            // no mill was closed
            int mask = move ^ Stones.closed(move);

            mover.move(stay, move, mask).normalize().analyze(m201 -> propagateLost(m201, score));
        };

    }

    void propagateLost(long i201, Score score) {
        PopCount clop = Positions.clop(i201);
        Slices<? extends MapSlice> slices = source.get(clop);
        int index = slices.posIndex(i201);
        int current = slices.scores.getScore(index);

        if(Score.isWon(current) && current <= score.value)
            return;

        MapSlice slice = slices.get(index);
        short offset = slice.offset(index);

        slice.submit(()->{});

    }
}
