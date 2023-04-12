package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.score.Score;
import org.junit.jupiter.api.Test;

import java.nio.ByteBuffer;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.stream.IntStream;

public class MovedGroupTest {
    static final IndexProvider indexes = IndexProvider.load();

    final PopCount p33 = PopCount.of(3,3);
    final Player w = Player.White;

    @Test
    public void process() {
        MovingGroups target = createGroups(p33, w, this::moved, this::closed);

        System.out.format("%9s: %9d\n", target.moved, target.moved.range());

        for(Score score = Score.LOST;true; score = score.next()) {

            IntStream tasks = target.propagate(target, score);
            if(tasks==null)
                break;

            int count = tasks.sum();

            System.out.format("%9s: %9d\n", score, count);
        }

        //MovedPosition pos = target.position(target, 562950021120016L);
        //pos.toString();
    }


    static MovingGroups createGroups(PopCount pop, Player player,
                                      Function<PopCount, ? extends ScoreTarget> moved,
                                      Function<PopCount, ? extends ScoreSet> closed) {

        ForkJoinTask<TargetGroup> movedTask = new RecursiveTask<>() {
            @Override
            protected TargetGroup compute() {
                return TargetGroup.create(pop, player, moved.andThen(TargetGroup::newSlices));
            }
        };

        ForkJoinTask<ClosingGroup> closedTask = new RecursiveTask<>() {
            @Override
            protected ClosingGroup compute() {
                return ClosingGroup.closed(pop, player, closed.andThen(ScoreSlices::of));
            }
        };

        ForkJoinTask.invokeAll(movedTask, closedTask);

        // todo: parallel
        return new MovingGroups(movedTask.join(), closedTask.join());
    }
    
    ScoreTarget moved(PopCount clop) {
        PosIndex index = indexes.build(p33, clop);
        ByteBuffer buffer = ByteBuffer.allocateDirect(index.range());
        return new ScoreTarget(index, Player.White, buffer);
    }

    ScoreSet closed(PopCount clop) {
        PosIndex index = indexes.build(p33, clop);
        return ConstSet.lost(index, w);
    }
}