package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.IntFunction;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 17:04
 */
public class Slices<Slice extends ScoreSlice> implements IndexLayer {

    public final ScoreSet scores;

    public final List<? extends Slice> slices;

    Slices(ScoreSet scores, List<? extends Slice> slices) {
        this.scores = scores;
        this.slices = slices;
    }

    public int size() {
        return slices.size();
    }

    public List<? extends Slice> slices() {
        return slices;
    }

    public Stream<? extends Slice> stream() {
        return slices.stream();
    }

    public Slice get(int posIndex) {
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

        for (ScoreSlice slice : slices)
            max = Math.max(max, slice.max());

        return max;
    }

    public void close() {
        slices.forEach(ScoreSlice::close);
        scores.close();
    }

    public static <Slice extends ScoreSlice> Slices<Slice>
    generate(ScoreSet scores, IntFunction<? extends Slice> slice) {
        int count = ScoreSlice.sliceCount(scores);
        List<Slice> slices = AbstractRandomList.generate(count, slice);
        return new Slices<>(scores, slices);
    }

    ScoreSet scores() {
        return scores;
    }

    @Override
    public PosIndex index() {
        return scores.index();
    }

    @Override
    public PopCount clop() {
        return scores.clop();
    }

    @Override
    public PopCount pop() {
        return scores.pop();
    }

    @Override
    public Player player() {
        return scores.player();
    }

    public int getScore(long i201) {
        int posIndex = scores.index.posIndex(i201);
        return get(posIndex).getScore(posIndex);
    }
}
