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
abstract public class ScoreSlices implements IndexLayer {

    abstract public ScoreSet scores();

    abstract List<? extends ScoreSlice> slices();

    public int size() {
        return slices().size();
    }

    public Stream<? extends ScoreSlice> stream() {
        return slices().stream();
    }

    public ScoreSlice get(int posIndex) {
        return slices().get(posIndex / MapSlice.SIZE);
    }

    public int posIndex(long i201) {
        return scores().posIndex(i201);
    }

    public String toString() {
        return String.format("ScoreSlices %s (%d)", scores(), max());
    }

    /**
     * Determine max score.
     * @return current max score of all slices.
     */
    public int max() {
        int max = 0;

        for (ScoreSlice slice : slices())
            max = Math.max(max, slice.max());

        return max;
    }

    public void close() {
        slices().parallelStream().forEach(ScoreSlice::close);
        scores().close();
    }

    @Override
    public PosIndex index() {
        return scores().index();
    }

    @Override
    public Player player() {
        return scores().player();
    }

    public int getScore(long i201) {
        int posIndex = scores().index.posIndex(i201);
        return get(posIndex).getScore(posIndex);
    }

    static ScoreSlices of(ScoreSet scores) {
        int size = ScoreSlice.sliceCount(scores);
        List<? extends ScoreSlice> slices = AbstractRandomList.generate(size, scores::openSlice);
        return new ScoreSlices() {

            @Override
            public ScoreSet scores() {
                return scores;
            }

            @Override
            List<? extends ScoreSlice> slices() {
                return slices;
            }
        };
    }
}
