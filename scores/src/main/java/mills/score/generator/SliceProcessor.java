package mills.score.generator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.11.19
 * Time: 12:47
 */

import mills.index.IndexProcessor;

/**
 * Process scores of a ScoreSlice and feed the results into target slices.
 */
abstract public class SliceProcessor implements IndexProcessor {

    final ScoreSlice source;

    final SlicesGroup<MapSlice> target;

    int score;

    public SliceProcessor(ScoreSlice source, SlicesGroup<MapSlice> target) {
        this.source = source;
        this.target = target;
    }

    /**
     * Process any position of given score.
     * @param score to analyze
     * @return count of processed positions
     */
    void process(int score) {
        this.score = score;
        source.processScores(this, score);
    }

    /**
     * Callback for each scored position.
     * @param posIndex
     * @param i201
     */
    abstract public void process(int posIndex, long i201);
}
