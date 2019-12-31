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
import java.util.function.BiConsumer;
import java.util.function.LongConsumer;
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

        DEBUG = score.value>1;

        BiConsumer<MovingGroup<?>, ScoreSlice> processors = (group, slice) -> {
            LongConsumer analyzer = m201 -> target.propagate(this, m201, score.next());
            IndexProcessor processor = group.processor(target, analyzer);
            slice.processScores(processor, score);
        };

        Stream<Runnable> movingTasks = this.propagate(score, processors);
        Stream<Runnable> closingTasks = closed.propagate(score, processors);

        Stream<? extends Runnable> tasks = join(closingTasks, movingTasks);
        if(tasks==null)
            return false;

        tasks.forEach(Runnable::run);

        return true;
    }

    static <T> Stream<? extends T> join(Stream<? extends T> a, Stream<? extends T> b) {
        if(a==null)
            return b;

        if(b==null)
            return a;

        return Stream.concat(a,b);

    }

    void propagate(MovedGroup source, long i201, Score newScore) {
        Clops clops = Positions.clops(i201);
        Slices<? extends MapSlice> slices = group.get(clops);
        ScoredPosition debug = debug(source, i201);
        int posIndex = slices.scores.index.posIndex(i201);
        MapSlice mapSlice = slices.get(posIndex);
        mapSlice.propagate(posIndex, i201, newScore.value);
    }

    @Override
    protected MovedPosition position(MovedGroup source, long i201) {
        List<? extends ScoredPosition> movedPositions = movedPositions(this, i201);
        List<? extends ScoredPosition> closedPositions = movedPositions(closed, i201);
        int score = getScore(i201);
        return new MovedPosition(i201, player, score, movedPositions, closedPositions);
    }

    /**
     * Return a list of positions if moving this i201 to moving group.
     * @param moving group either moved or closed.
     * @param i201 current position to analyze.
     * @return a List of moved positions.
     */
    protected List<? extends ScoredPosition> movedPositions(MovingGroup<?> moving, long i201) {

        boolean swap = moving.player==Player.White;
        Mover mover = Moves.moves(jumps()).mover(swap);

        // playing forward
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int free = Stones.STONES ^ (stay|move);

        // those positions will close a mill
        int closes = Stones.closes(move) & ~stay;

        if (!moving.closed())
            free &= ~closes;
        else
            free &= closes;

        //ScoredPosition debug = debug(target, i201);
        return mover.move(stay, move, move, free).normalize().transform(m201 -> moving.position(this, m201));
    }
}
