package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Stones;

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
            tasks.forEach(ForkJoinTask::invoke);
            //ForkJoinTask.invokeAll(tasks);
        }

        Long i201 = self.moved.group.get(Clops.of(self.moved.pop(), PopCount.EMPTY)).index().i201(0);
        MovedPosition pos = movedPosition(i201, Player.White);

        return tasks.size();
    }

    Function<ScoreSlice, RecursiveAction> task(Score score, boolean close) {
        return slice -> new RecursiveAction() {

            @Override
            protected void compute() {
                IndexProcessor processor = other.processor(slice.player(), score, close);
                slice.processScores(processor, score);
            }
        };
    }

    public MovedPosition movedPosition(long i201, Player player) {
        boolean inverted = player!=this.player();
        long j201 = inverted ? Positions.inverted(i201) : i201;
        Clops clops = Positions.clops(j201);
        ScoreSet scores = self.moved.group.get(clops).scores;
        return new MovedPosition(scores, i201, player, null);
    }

    public ScoredPosition closedPosition(long i201, Player player) {
        return self.closed.position(i201, player);
    }

    public class MovedPosition extends ScoredPosition {

        final List<MovedPosition> moved;
        final List<ScoredPosition> closed;

        @Override
        protected MovedPosition position(long i201, Player player, ScoredPosition inverted) {
            return new MovedPosition(scores, i201, player, inverted);
        }

        public MovedPosition(ScoreSet scores, long i201, Player player, ScoredPosition inverted) {
            super(scores, i201, player, inverted);

            moved = moved();
            closed = closed();
        }

        List<MovedPosition> moved() {
            Mover mover = scores.mover(player.other());

            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            mover.move(stay, move, move^closed);

            return mover.transform(i201->movedPosition(i201, player));
        }

        List<ScoredPosition> closed() {
            Mover mover = scores.mover(player.other());

            int stay = Stones.stones(i201, player.other());
            int move = Stones.stones(i201, player);
            int closed = Stones.closed(move);
            mover.move(stay, move, closed);

            return mover.transform(i201->closedPosition(i201, player));
        }
    }
}