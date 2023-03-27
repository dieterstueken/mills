package mills.score.generator;

import mills.score.Score;
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
        slices.parallelStream().forEach(TargetSlice::init);
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
            ScoreTarget scores = scores();

            int won = 0;
            int lost = 0;
            int dawn = 0;

            for(int posIndex=0; posIndex<scores.size(); ++posIndex) {
                int score = scores.getScore(posIndex);
                if(Score.isWon(score))
                    ++won;
                else if(Score.isLost(score))
                    ++lost;
                else
                    ++dawn;
            }

            int pnd = slices().stream().mapToInt(s->s.pending).reduce(0, Integer::max);

            LOGGER.log(Level.INFO, String.format("finished %s D:%,d W:%,d L:%,d M:%d P:%d",
                    scores, dawn, won, lost, max(), pending()));
        }
    }
}
