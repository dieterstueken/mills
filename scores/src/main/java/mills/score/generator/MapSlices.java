package mills.score.generator;

import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.01.20
 * Time: 10:29
 */
public class MapSlices extends ScoreSlices<ScoreMap> {

    MapSlices(ScoreSet scores, List<? extends ScoreSlice<ScoreMap>> slices) {
        super(scores, slices);
    }

    static MapSlices of(ScoreMap scores) {
        int size = ScoreSlice.sliceCount(scores);
        List<? extends MapSlice> slices = AbstractRandomList.generate(size, index -> MapSlice.of(scores, index));
        return new MapSlices(scores, slices);
    }

    int init() {
        if(!scores.canJump()) {
            return slices.parallelStream().mapToInt(ScoreSlice::init).sum();
        } else
            return 0;
    }
}
