package mills.score.generator;

import mills.score.Score;

import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.01.20
 * Time: 18:01
 */
class GroupGenerator {

    final MovingGroups self;

    final MovingGroups other;

    GroupGenerator(MovingGroups self, MovingGroups other) {
        this.self = self;
        this.other = other;
    }

    public Stream<? extends ScoreMap> generate() {
        System.out.format("%9s: %9d\n", self.moved, self.moved.range());
        if(other!=self)
            System.out.format("%9s: %9d\n", other.moved, other.moved.range());

        for (Score score = Score.LOST; true; score = score.next()) {

            IntStream tasks = self.propagate(other, score);
            if(self!=other)
                tasks = MovingGroups.concat(tasks, other.propagate(self, score));

            if (tasks == null)
                break;

            int count = tasks.sum();

            System.out.format("%9s: %9d\n", score, count);
        }

        Stream<? extends MapSlices> slices = self.moved.group.values().parallelStream();
        if (other != self)
            slices = Stream.concat(slices, other.moved.group.values().parallelStream());

        return slices.peek(MapSlices::close)
                .map(MapSlices::scores);
    }
}
