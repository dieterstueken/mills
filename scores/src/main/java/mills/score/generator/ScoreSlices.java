package mills.score.generator;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 17:04
 */
public class ScoreSlices {

    public final ScoreSet scores;

    public final List<MapSlice> slices;

    private ScoreSlices(ScoreSet scores, List<MapSlice> slices) {
        this.scores = scores;
        this.slices = slices;
    }

    public String toString() {
        return String.format("ScoreSlices %s (%d)", scores, max());
    }

    public static ScoreSlices of(ScoreMap scores) {
        return new ScoreSlices(scores, MapSlice.slices(scores));
    }

    /**
     * Lookup a score based on a i201 mask.
     *
     * @param i201 position to look up.
     * @return current score or count down
     */
    public int getScore201(long i201) {
        int index = scores.posIndex(i201);
        final MapSlice slice = slices.get(index >>> 15);
        final short offset = (short) (index & Short.MAX_VALUE);
        return slice.getScore(offset);
    }

    public MapSlice getSlice(int posIndex) {
        return slices.get(posIndex / MapSlice.SIZE);
    }

    /**
     * Determine max value.
     * Has side effects:
     * Finish all pending actions.
     * Update map.max value.
     *
     * @return current max score of all slices.
     */
    public int max() {
        int max = 0;
        for (MapSlice slice : slices) {
            int m = slice.max();
            if(m>max)
                max = m;
        }

        return max;
    }

    public void close() {
        slices.forEach(MapSlice::close);
        scores.close();
    }
}
