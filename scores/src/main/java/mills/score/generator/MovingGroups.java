package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Positions;
import mills.score.Score;

import java.util.function.LongConsumer;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.12.19
 * Time: 21:18
 */
public class MovingGroups {
    
    final MovingGroup<? extends TargetSlices> moved;
    final MovingGroup<? extends ScoreSlices> closed;

    public MovingGroups(MovingGroup<? extends TargetSlices> moved, MovingGroup<? extends ScoreSlices> closed) {
        this.moved = moved;
        this.closed = closed;
    }

    public IntStream propagate(MovingGroups target, Score score) {

        //if(score.value>3)
        //    DEBUG = true;

        Score next = score.next();
        LongConsumer analyzer = m201 -> target.propagate(this, m201, next);
        Player targetPlayer = target.moved.player;

        IntStream movingTasks = moved.propagate(score, targetPlayer, analyzer);
        IntStream closingTasks = closed.propagate(score, targetPlayer, analyzer);

        return concat(closingTasks, movingTasks);
    }

    static IntStream concat(IntStream a, IntStream b) {
        if(a==null)
            return b;

        if(b==null)
            return a;

        return IntStream.concat(a,b);

    }

    void propagate(MovingGroups source, long i201, Score newScore) {
        PopCount clop = Positions.clop(i201);
        TargetSlices slices = moved.group.get(clop);
        if(slices!=null) {
            int posIndex = slices.scores().index.posIndex(i201);
            TargetSlice mapSlice = slices.get(posIndex);
            mapSlice.propagate(posIndex, i201, newScore.value);
            //ScoredPosition debug = debug(source, i201);
        }
    }
}
