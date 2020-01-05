package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;

import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.function.LongConsumer;
import java.util.function.ToIntBiFunction;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 21:18
 */
public class MovingGroups {
    
    final MovingGroup<? extends MapSlices> moved;
    final MovingGroup<? extends ScoreSlices> closed;

    public MovingGroups(MovingGroup<? extends MapSlices> moved, MovingGroup<? extends ScoreSlices> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    public static MovingGroups create(PopCount pop, Player player,
                               Function<PopCount, ? extends ScoreMap> moved,
                               Function<PopCount, ? extends ScoreSet> closed) {

        ForkJoinTask<MovingGroup<? extends MapSlices>> movedTask = new RecursiveTask<>() {
            @Override
            protected MovingGroup<? extends MapSlices> compute() {
                return MovingGroup.create(pop, player, moved);
            }
        };

        ForkJoinTask<MovingGroup<? extends ScoreSlices>> closedTask = new RecursiveTask<>() {
                    @Override
                    protected MovingGroup<? extends ScoreSlices> compute() {
                        return ClosingGroup.closed(pop, player, closed);
                    }
                };

        ForkJoinTask.invokeAll(movedTask, closedTask);

        // todo: parallel
        return new MovingGroups(movedTask.join(), closedTask.join());
    }

    public int propagate(MovingGroups target, Score score) {

        //if(score.value>3)
        //    DEBUG = true;

        ToIntBiFunction<MovingGroup<?>, ScoreSlice> processors = (group, slice) -> {
            LongConsumer analyzer = m201 -> target.propagate(this, m201, score.next());
            IndexProcessor processor = group.processor(target, analyzer);
            return slice.processScores(processor, score);
        };

        IntStream movingTasks = moved.propagate(score, processors);
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

    void propagate(MovingGroups source, long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        MapSlices slices = moved.group.get(clops);

        int posIndex = slices.scores().index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        mapSlice.propagate(posIndex, i201, newScore.value);
        //ScoredPosition debug = debug(source, i201);
    }

    //protected MovedPosition position(MovingGroups source, long i201) {
    //    List<? extends ScoredPosition> closedPositions = movedPositions(closed, i201);
    //    List<? extends ScoredPosition> movedPositions = movedPositions(moved, i201);
    //    int score = source.moved.getScore(i201);
    //    return new MovedPosition(i201, source.moved.player, score, movedPositions, closedPositions);
    //}

    /**
     * Return a list of positions if moving this i201 to moving group.
     * @param target group either moved or closed.
     * @param i201 current position to analyze.
     * @return a List of moved positions.
     */
    //protected List<? extends ScoredPosition> movedPositions(MovingGroups target, long i201) {

    //    boolean swap = moving.player==Player.White;
    //    boolean close = moving.closed();

    //    Mover mover = new Mover(Moves.moves(moved.jumps()),swap) {
    //        @Override
    //        public boolean process(int stay, int move, int mask) {
    //            int moved = move^mask;
    //            int closed = Stones.closed(moved) & mask;
    //            if((closed!=0) == close)
    //                super.process(stay, move, mask);

    //            return !Moves.ABORT;
    //        }
    //    };

    //    // playing forward
    //    int stay = Stones.stones(i201, moving.player.other());
    //    int move = Stones.stones(i201, moving.player);

    //    //ScoredPosition debug = debug(target, i201);
    //    mover.move(stay, move, move).normalize();

    //    //Position pos = Position.of(i201);
    //    //mover.analyze(m201 -> {
    //    //    Position moved = Position.of(m201);
    //    //});

    //    return mover.transform(m201 -> moving.position(moving, m201, moving.player.other()));
    //}
}
