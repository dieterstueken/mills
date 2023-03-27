package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
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

    public MovingGroup(PopCount pop, Player player, Stream<Slices> slices) {
        super(pop, player, slices);
    }

    public static Set<PopCount> clops(PopCount pop) {
        PopCount mclop = pop.mclop().min(PopCount.P99.sub(pop).swap());

        Set<PopCount> clops = new TreeSet<>();

        for (PopCount clop : PopCount.TABLE) {
            if(clop.le(mclop))
                clops.add(clop);
        }

        return clops;
    }

    public static MovingGroup<TargetSlices> create(PopCount pop, Player player, Stream<? extends ScoreTarget> scores) {
        return new MovingGroup<>(pop, player, scores.map(TargetSlices::of));
    }

    public static MovingGroup<TargetSlices> create(PopCount pop, Player player, Function<PopCount, ? extends ScoreTarget> generator) {
        Stream<ScoreTarget> targets = clops(pop).parallelStream().map(generator);
        return create(pop, player, targets);
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

        // backtrace moves: move Black
        boolean swap = targetPlayer!=Player.White;
        Mover mover = Moves.moves(canJump()).mover(swap);

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.opponent());
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
