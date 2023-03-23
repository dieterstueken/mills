package mills.score.generator;

import mills.bits.Player;
import mills.index.PosIndex;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 17:04
 */
public class ScoreSlices<Scores extends ScoreSet> {

    protected ScoreSet scores;

    protected List<? extends ScoreSlice<Scores>> slices;

    ScoreSlices(ScoreSet scores, List<? extends ScoreSlice<Scores>> slices) {
        this.scores = scores;
        this.slices = slices;
    }

    public int size() {
        return slices.size();
    }

    public Stream<? extends ScoreSlice<Scores>> stream() {
        return slices.stream();
    }

    public ScoreSlice<Scores> get(int posIndex) {
        return slices.get(posIndex / MapSlice.SIZE);
    }

    public int posIndex(long i201) {
        return scores.posIndex(i201);
    }

    public String toString() {
        return String.format("ScoreSlices %s (%d)", scores, max());
    }

    /**
     * Determine max score.
     * @return current max score of all slices.
     */
    public int max() {
        int max = 0;

        for (var slice : slices)
            max = Math.max(max, slice.max());

        return max;
    }

    public PosIndex index() {
        return scores.index();
    }

    public Player player() {
        return scores.player();
    }

    public int getScore(long i201) {
        int posIndex = scores.index.posIndex(i201);
        return get(posIndex).getScore(posIndex);
    }

    static ScoreSlices<ScoreSet> of(ScoreSet scores) {
        int size = ScoreSlice.sliceCount(scores);
        List<? extends ScoreSlice<ScoreSet>> slices = AbstractRandomList.generate(size, index -> new ScoreSlice<>(scores, index));
        return new ScoreSlices<>(scores, slices);
    }
}
