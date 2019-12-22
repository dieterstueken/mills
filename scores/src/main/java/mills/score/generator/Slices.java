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

    public final ScoreLayer scores;

    public final List<? extends Slice> slices;

    Slices(ScoreLayer scores, List<? extends Slice> slices) {
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
     * Lookup a score based on a i201 mask.
     *
     * @param i201 position to look up.
     * @return current score or count down
     */
    public int getScore201(long i201) {
        int index = scores.posIndex(i201);
        Slice slice = get(index);
        return slice.getScore(index);
    }

    public int getScore(int posIndex) {
        return scores.getScore(posIndex);
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
    generate(ScoreLayer scores, IntFunction<? extends Slice> slice) {
        int count = ScoreSlice.sliceCount(scores);
        List<Slice> slices = AbstractRandomList.generate(count, slice);
        return new Slices<>(scores, slices);
    }

    ScoreLayer scores() {
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
}
