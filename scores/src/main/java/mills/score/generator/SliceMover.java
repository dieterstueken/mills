package mills.score.generator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.19
 * Time: 13:38
 */
public class SliceMover extends SliceProcessor {

    public SliceMover(ScoreSlice source, SlicesGroup<MapSlice> target) {
        super(source, target);
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

    public void process(int posIndex, long i201) {
        // todo:
    }
}
