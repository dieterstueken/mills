package mills.score.generator;

import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.01.20
 * Time: 10:29
 */
public abstract class MapSlices extends ScoreSlices {

    abstract public ScoreMap scores();

    abstract List<? extends TargetSlice> slices();

    public MapSlice get(int posIndex) {
        return slices().get(posIndex / MapSlice.SIZE);
    }

    static MapSlices of(ScoreMap scores) {
        int size = ScoreSlice.sliceCount(scores);
        List<MapSlice<ScoreMap>> slices = AbstractRandomList.generate(size, scores::openSlice);
        return new MapSlices() {

            @Override
            public ScoreMap scores() {
                return scores;
            }

            @Override
            List<? extends TargetSlice> slices() {
                return slices;
            }
        };
    }

    int init() {
        if(!scores().canJump()) {
            return slices().parallelStream().mapToInt(MapSlice::init).sum();
        } else
            return 0;
    }
}
