package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 21:18
 */
public class MovedGroup extends MovingGroup<MapSlice> {
    
    final ClosedGroup closed;
    
    public MovedGroup(PopCount pop, Player player, ClosedGroup closed, Stream<ScoreMap> slices) {
        super(pop, player, slices.map(scores -> Slices.generate(scores, scores::openSlice)));
        this.closed = closed;
    }

    public static MovedGroup moved(PopCount pop, Player player, ClosedGroup closed, Function<Clops, ScoreMap> generator) {
        PopCount mclop = pop.mclop()
                        .min(PopCount.P99.sub(pop).swap());

        Stream<ScoreMap> slices = PopCount.TABLE.parallelStream()
                        .filter(clop -> clop.le(mclop))
                        .map(clop -> Clops.of(pop, clop))
                        .map(generator);

        return new MovedGroup(pop, player, closed, slices);
    }

    public int propagate(MovedGroup target, Score score) {

        //if(score.value>3)
        //    DEBUG = true;

        ToIntBiFunction<MovingGroup<?>, ScoreSlice> processors = (group, slice) -> {
            LongConsumer analyzer = m201 -> target.propagate(this, m201, score.next());
            IndexProcessor processor = group.processor(target, analyzer);
            return slice.processScores(processor, score);
        };

        IntStream movingTasks = this.propagate(score, processors);
        IntStream closingTasks = closed.propagate(score, processors);

        IntStream tasks = concat(closingTasks, movingTasks);
        if(tasks==null)
            return 0;

        long count = tasks.sum();

        return (int) count;
    }

    static IntStream concat(IntStream a, IntStream b) {
        if(a==null)
            return b;

        if(b==null)
            return a;

        return IntStream.concat(a,b);

    }

    void propagate(MovedGroup source, long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        Slices<? extends MapSlice> slices = group.get(clops);

        int posIndex = slices.scores.index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        int score = mapSlice.propagate(posIndex, i201, newScore.value);
        ScoredPosition debug = debug(source, i201);
    }

    @Override
    protected MovedPosition position(MovedGroup source, long i201) {
        List<? extends ScoredPosition> closedPositions = movedPositions(closed, i201);
        List<? extends ScoredPosition> movedPositions = movedPositions(this, i201);
        int score = source.getScore(i201);
        return new MovedPosition(i201, source.player, score, movedPositions, closedPositions);
    }

    /**
     * Return a list of positions if moving this i201 to moving group.
     * @param moving group either moved or closed.
     * @param i201 current position to analyze.
     * @return a List of moved positions.
     */
    protected List<? extends ScoredPosition> movedPositions(MovingGroup<?> moving, long i201) {

        boolean swap = moving.player==Player.White;
        boolean close = moving.closed();

        Mover mover = new Mover(Moves.moves(jumps()),swap) {
            @Override
            public boolean process(int stay, int move, int mask) {
                int moved = move^mask;
                int closed = Stones.closed(moved) & mask;
                if((closed!=0) == close)
                    super.process(stay, move, mask);

                return !Moves.ABORT;
            }
        };

        // playing forward
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);

        //ScoredPosition debug = debug(target, i201);
        mover.move(stay, move, move).normalize();

        //Position pos = Position.of(i201);
        //mover.analyze(m201 -> {
        //    Position moved = Position.of(m201);
        //});

        return mover.transform(m201 -> moving.position(this, m201, player.other()));
    }
}
