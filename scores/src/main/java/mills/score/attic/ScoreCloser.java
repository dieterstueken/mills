package mills.score.attic;

import mills.index.IndexProcessor;
import mills.score.Pair;
import mills.score.Score;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.06.13
 * Time: 21:04
 */
public class ScoreCloser extends ScoreWorker {

    final int score;

    public ScoreCloser(ScoreSlices source, ScoreSlices target, int score) {
        super(source, target);
        this.score = score;
    }

    protected Closer processor() {
        return new Closer();
    }

    protected void processSlice(IndexProcessor processor, ScoreSlice slice) {
        slice.processAny(processor, score);
    }

    /**
     * Process all lost closings of given score and replace them by a count down.
     * On countdown == 0 the score value is resolved.
     * Won closings stay unresolved.
     */
    class Closer extends Processor {

        final Move count = Move.forward(source.map, target.map);

        boolean resolved(int current) {
            // either it is won or any resolved lost
            return Score.isWon(current) || (current>0 && current<score);
        }

        @Override
        boolean analyze(final ScoreSlice slice, short offset, long i201) {
            int current = slice.getScore(offset);

            if(current!=score)
                return false;

            int count = this.count.level(i201).size();

             // closed score == 1 -> 0
            if(count>0) // start countdown
                slice.setScore(offset, -count);
            else            // leave as resolved but mark dirty again
                slice.any[score] |= ScoreSlice.mask(offset);

            return true;
        }
    }

    static ScoreCloser of(Pair<ScoreSlices> input, int score) {
        return new ScoreCloser(input.other, input.self, score);
    }
}
