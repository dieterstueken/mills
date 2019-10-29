package mills.score.generator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.10.19
 * Time: 19:48
 */
public class ScoreWorker {

    final SliceGroup source;

    final SliceGroup target;

    public ScoreWorker(SliceGroup source, SliceGroup target) {
        this.source = source;
        this.target = target;
    }
}
