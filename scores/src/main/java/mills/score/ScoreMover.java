package mills.score;

import mills.index.IndexProcessor;

import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 17:59
 */
public class ScoreMover extends ScoreWorker {

    final int score;

    ScoreMover(ScoreSlices source, ScoreSlices target, int score) {
        super(source, target);

        this.score = score;
    }

    protected IndexProcessor processor() {
        return new SliceMover();
    }

    protected void processSlice(IndexProcessor processor, ScoreSlice slice) {
        slice.processAny(processor, score);
    }

    class SliceMover extends Processor {

        final Move move = Move.reverse(source.map, target.map);

        final Move.Analyzer analyzer = Score.isLost(score) ? new MoveLost() : new MoveWon();

        @Override
        public boolean analyze(ScoreSlice slice, short offset, long i201) {

            int current = slice.getScore(offset);

            if (current != score)
                return false;

            move.level(i201).analyze(analyzer);

            return true;
        }
    }

    abstract class Analyzer implements Move.Analyzer {

        final int newScore = score+1;

        @Override
        public void analyze(long i201) {
            final int index = target.map.posIndex((i201));
            final ScoreSlice slice = target.getSlice(index);
            final short offset = slice.offset(index);

            propagate(slice, offset, i201);
        }

         // default implementation propagates lost(score) -> won(score+1)
        abstract void propagate(ScoreSlice slice, final short offset, final long i201);
    }

    class MoveLost extends Analyzer {

        // return if current target value may stay
        boolean resolved(int current) {
            // any shorter won path is left untouched
            return Score.isWon(current) && current <= newScore;
        }

        // propagate lost(score) -> won(score+1)
        void propagate(ScoreSlice slice, final short offset, final long i201) {
            final int current = slice.getScore(offset);
            if(resolved(current))
                return;

            slice.submit(new Consumer<>() {
                @Override
                public void accept(ScoreSlice slice) {
                    final int current = slice.getScore(offset);
                    if (!resolved(current))
                        slice.setScore(offset, newScore);
                }

                public String toString() {
                    return String.format("propagate %d W(%d)", offset, newScore);
                }
            });
        }
    }

    class MoveWon extends Analyzer {

        // count forward moves
        final Move count = Move.forward(target.map, source.map);

        // return if current value may persist
        boolean resolved(int current) {
            // any win and all indifferent lost closes > newScore are left untouched
            return Score.isWon(current) || current > newScore;
        }

        // process won -> lost
        @Override
        void propagate(final ScoreSlice slice, final short offset, final long i201) {
            final int current = slice.getScore(offset);
            if(resolved(current))
                return;

            // must be analyzed
            // calculate unresolved if current==0
            final int unresolved = current == 0 ? this.count.move(i201).size() : 0;

            slice.submit(new Consumer<>() {
                @Override
                public String toString() {
                    return String.format("move %d L(%d)", offset, newScore);
                }

                @Override
                public void accept(ScoreSlice slice) {
                    final int current = slice.getScore(offset);

                    if (current == 0) {
                        // must be at least 1 since we just propagate a position.
                        if (unresolved < 1) {
                            String error = String.format("no unresolved for %d@%s", newScore, slice);
                            throw new IllegalStateException(error);
                        }

                        if (unresolved == 1)
                            slice.setScore(offset, newScore);
                        else
                            slice.setScore(offset, 1 - unresolved);
                    } else if (current == -1) {
                        // count down finished
                        slice.setScore(offset, newScore);
                    } else if (current < -1) {
                        // continue count down
                        slice.setScore(offset, current + 1);
                    } else if (!resolved(current)) {
                        slice.setScore(offset, newScore);
                    }
                }
            });
        }
    }

    static ScoreMover of(Pair<ScoreSlices> input, int score) {
        return new ScoreMover(input.other, input.self, score);
    }
}
