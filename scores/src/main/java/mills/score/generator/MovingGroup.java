package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.ToIntBiFunction;
import java.util.stream.Collectors;
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

    public static MovingGroup<MapSlices> create(PopCount pop, Player player, Function<PopCount, ? extends ScoreMap> generator) {
        PopCount mclop = pop.mclop().min(PopCount.P99.sub(pop).swap());

        Stream<MapSlices> mapSlices = PopCount.TABLE.parallelStream()
                .filter(clop -> clop.le(mclop))
                .map(generator)
                .map(MapSlices::of);

        return new MovingGroup<>(pop, player, mapSlices);
    }

    public IntStream propagate(Score score, ToIntBiFunction<MovingGroup<?>, ScoreSlice> processors) {

        List<? extends ScoreSlice> slices = group.values().stream()
                   .flatMap(ScoreSlices::stream)
                   .filter(slice->slice.hasScores(score))
                   .collect(Collectors.toList());

        if(slices.isEmpty())
            return null;

        return slices.parallelStream() //parallelStream()
                .filter(slice -> slice.any(score))
                .mapToInt(slice -> processors.applyAsInt(this, slice));
    }

    /**
     * Create an IndexProcessor to process Scores of score for each of our slices,
     * The moved positions are then propagated to MovedGroup target.
     * @param target receiving new scores.
     * @param analyzer to analyze moved positions.
     * @return an IndexProcessor to process a single slice.
     */
    IndexProcessor processor(MovingGroups target, LongConsumer analyzer) {

        // backtrace moves: move Black
        boolean swap = target.moved.player()!=Player.White;
        Mover mover = Moves.moves(jumps()).mover(swap);

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.other());
            int mask = Stones.closed(move);
            if (!closing())
                mask ^= move;
            //Position pos = Position.of(m201);
            mover.move(stay, move, mask).normalize().analyze(analyzer::accept);
        };
    }
}
