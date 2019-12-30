package mills.score.generator;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.score.Score;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroupGenerators {

    final GroupGenerator self;
    final GroupGenerator other;

    public GroupGenerators(GroupGenerator self, GroupGenerator other) {
        this.self = self;
        this.other = other;
    }

    public GroupGenerators(GroupGenerator both) {
        this(both, both);
    }

    public Player player() {
        return self.moved.player();
    }

    public int process(Score score) {

        List<ForkJoinTask<?>> tasks = Stream.concat(
                self.moved.slices(score).map(task(score, false)),
                self.closed.slices(score).map(task(score, true))
        ).collect(Collectors.toList());

        if (!tasks.isEmpty()) {
            for (ForkJoinTask<?> task : tasks) {
                task.invoke();
            }
            //ForkJoinTask.invokeAll(tasks);
        }

        return tasks.size();
    }

    Function<ScoreSlice, RecursiveAction> task(Score score, boolean close) {
        return slice -> new RecursiveAction() {

            @Override
            protected void compute() {
                IndexProcessor processor = other.processor(slice, score, close);
                slice.processScores(processor, score);
            }
        };
    }
}