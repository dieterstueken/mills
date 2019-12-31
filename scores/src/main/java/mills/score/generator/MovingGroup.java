package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 18:09
 */
public class MovingGroup<Slice extends ScoreSlice> extends SlicesGroup<Slice> {

    boolean DEBUG = true;

    public boolean closed() {
        return false;
    }

    public MovingGroup(PopCount pop, Player player, List<Slices<Slice>> slices) {
        super(pop, player, slices);
    }

    ScoredPosition debug(MovedGroup target, long i201) {
        return DEBUG ? position(target, i201) : null;
    }

    public Stream<Runnable> propagate(Score score, BiConsumer<MovingGroup<?>, ScoreSlice> processors) {
        List<? extends ScoreSlice> slices = group.values().stream()
                   .flatMap(Slices::stream)
                   .filter(slice->slice.hasScores(score))
                   .collect(Collectors.toList());

        if(slices.isEmpty())
            return null;
           
        return slices.stream() //parallelStream()
                .filter(slice -> slice.any(score))
                .map(slice -> () -> processors.accept(this, slice));
    }

    /**
     * Create an IndexProcessor to process Scores of score for each of our slices,
     * The moved positions are then propagated to MovedGroup target.
     * @param target receiving new scores.
     * @param analyzer to analyze moved positions.
     * @return an IndexProcessor to process a single slice.
     */
    IndexProcessor processor(MovedGroup target, LongConsumer analyzer) {

        // backtrace moves: move Black
        boolean swap = target.player()!=Player.Black;
        Mover mover = Moves.moves(jumps()).mover(swap);

        return (posIndex, i201) -> {
            // reversed move
            int stay = Stones.stones(i201, player);
            int move = Stones.stones(i201, player.other());
            int mask = Stones.closed(move);
            if (!closed())
                mask ^= move;
            ScoredPosition debug = debug(target, i201);
            mover.move(stay, move, mask).normalize().analyze(analyzer);
        };
    }

    protected ScoredPosition position(MovedGroup target, long i201, Player player) {
        if(player == this.player)
            return position(target, i201);

        i201 = Positions.inverted(i201);
        return position(target, i201).inverted();
    }

    /**
     * This is a stub until ClosedPosition will return MovedPosition, too.
     * @param source
     * @param i201
     * @return
     */
    protected ScoredPosition position(MovedGroup source, long i201) {
        return position(i201);
    }

}
