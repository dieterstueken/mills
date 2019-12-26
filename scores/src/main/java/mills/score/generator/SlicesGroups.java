package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.position.Positions;
import mills.score.Score;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.LongConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SlicesGroups {

    final LayerGroup<Slices<? extends MapSlice>> moved;

    final LayerGroup<Slices<? extends ScoreSlice>> closed;

    public SlicesGroups(ScoreGroups scores) {
        this.moved = scores.moved.map(ScoreMap::slices);
        this.closed = scores.closed.map(ScoreSet::slices);
    }

    class Tracer {

        final LayerGroup<? extends Slices<? extends MapSlice>> target;

        Tracer(LayerGroup<? extends Slices<? extends MapSlice>> target) {
            this.target = target;
        }

        public int process(Score score) {

            List<ForkJoinTask<?>> tasks = Stream.concat(
                    tasks(moved, score, false),
                    tasks(closed, score, true)
            ).collect(Collectors.toList());

            if (!tasks.isEmpty()) {
                tasks.forEach(ForkJoinTask::invoke);
                //ForkJoinTask.invokeAll(tasks);
            }

            return tasks.size();
        }

        Stream<ForkJoinTask<?>> tasks(LayerGroup<? extends Slices<?>> group, Score score, boolean closed) {
            return group.stream()
                    .flatMap(Slices::stream)
                    .filter(slice -> slice.hasScores(score))
                    .map(slice -> new RecursiveAction() {
                        @Override
                        protected void compute() {
                            IndexProcessor processor = processor(slice.player(), score, closed);
                            slice.processScores(processor, score);
                        }
                    });
            };

        IndexProcessor processor(Player player, Score score, boolean closed) {

            boolean swap = player.equals(moved.player());
            Mover mover = Moves.moves(moved.jumps()).mover(swap);
            Score newScore = score.next();
            LongConsumer analyzer = m201 -> propagate(m201, newScore);

            return (posIndex, i201) -> {
                // reversed move
                int stay = Stones.stones(i201, player);
                int move = Stones.stones(i201, player.other());
                int mask = Stones.closed(move);
                if(!closed)
                    mask ^= move;
                mover.move(stay, move, mask).normalize().analyze(analyzer);
            };
        }

        void propagate(long i201, Score newScore) {
            Clops clops = Positions.clops(i201);
            Slices<? extends MapSlice> slices = moved.group.get(clops);
            int posIndex = slices.scores.index.posIndex(i201);
            MapSlice mapSlice = slices.get(posIndex);
            mapSlice.propagate(posIndex, i201, newScore.value);
        }
    }
}
