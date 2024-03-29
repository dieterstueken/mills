package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.Map;
import java.util.function.LongConsumer;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:09
 */
public class MovingGroup<Slices extends ScoreSlices> extends LayerGroup<Slices> {

    public boolean closing() {
        return false;
    }

    public MovingGroup(PopCount pop, Player player, Map<PopCount, ? extends Slices> group) {
        super(pop, player, group);
    }

    public static Stream<PopCount> clops(PopCount pop) {
        PopCount mclop = pop.mclop(true);
        return PopCount.CLOPS.stream().filter(mclop::ge);
    }

    /**
     * Return a stream of propagations to perform. Each returns a count of propagations.
     * The stream may be performed in parallel.
     *
     * @param score to analyze.
     * @param targetPlayer next player of target level.
     * @param analyzer receiving i201 positions.
     * @return an IntStream to be processed.
     */
    public IntStream propagate(Score score, Player targetPlayer, LongConsumer analyzer) {

        List<? extends ScoreSlice<?>> slices = group.values().stream()
                .flatMap(ScoreSlices::stream)
                .filter(slice -> slice.any(score)).toList();

        // collect into a temporary array.
        // concat is null transparent.
        if(slices.isEmpty())
            return null;

        return slices.parallelStream() //parallelStream()
                .mapToInt(slice -> slice.processScores(processor(targetPlayer, analyzer), score));
    }

    /**
     * Create an IndexProcessor to process Scores of score for each of our slices,
     * The moved positions are then propagated to MovedGroup target.
     * @param targetPlayer of target to receive the result.
     * @param analyzer to analyze moved positions.
     * @return an IndexProcessor to process a single slice.
     */
    IndexProcessor processor(Player targetPlayer, LongConsumer analyzer) {

        // backtrace: moved by opponent.
        Player opponent = player.opponent();

        // backtrace moves: move Black
        boolean swap = targetPlayer!=Player.White;

        Mover mover = Moves.moves(opponent.canJump(pop)).mover(swap);

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, opponent);
            int mask = Stones.closed(move);
            if (!closing())
                mask ^= move;
            //Position pos = Position.of(m201);
            mover.move(stay, move, mask).normalize().analyze(analyzer);
        };
    }

    public int count() {
        int count = 0;
        for (Slices slices : group.values()) {
            count += slices.slices().size();
        }
        return count;
    }


    public int range() {
        int range=0;

        for (Slices slices : group.values()) {
            range += slices.index().range();
        }

        return range;
    }

    public int max(ToIntFunction<? super Slices> count) {
        return group.values().stream().mapToInt(count).reduce(0, Integer::max);
    }
}
