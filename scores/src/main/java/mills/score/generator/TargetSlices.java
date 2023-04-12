package mills.score.generator;

import mills.util.AbstractRandomList;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.01.20
 * Time: 10:29
 */
public abstract class TargetSlices extends ScoreSlices {

    static final Logger LOGGER = Logger.getLogger(TargetSlices.class.getName());

    abstract public ScoreTarget scores();

    abstract List<? extends TargetSlice> slices();

    public TargetSlice get(int posIndex) {
        return slices().get(posIndex / MapSlice.SIZE);
    }

    static TargetSlices of(ScoreTarget scores) {
        int size = ScoreSlice.sliceCount(scores);
        List<TargetSlice> slices = AbstractRandomList.generate(size, scores::openSlice);
        //slices.parallelStream().forEach(TargetSlice::init);
        return new TargetSlices() {

            @Override
            public ScoreTarget scores() {
                return scores;
            }

            @Override
            List<? extends TargetSlice> slices() {
                return slices;
            }
        };
    }

    public void close() {
        slices().parallelStream().forEach(TargetSlice::close);
        log();
    }

    int pending() {
        return slices().stream().mapToInt(TargetSlice::pending).reduce(0, Integer::max);
    }

    private void log() {
        if(LOGGER.isLoggable(Level.INFO)) {
            scores().stat(Level.INFO);
            LOGGER.log(Level.INFO, String.format("finished %s M:%d P:%d", scores(), max(), pending()));
        }
    }
}
