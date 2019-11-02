package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:15
 */
public class SlicesGroup<Slice extends ScoreSlice> extends Group<Slices<? extends Slice>> implements Layer {

    static SlicesGroup<ScoreSlice> open(ScoreSet scores) {
        SlicesGroup<ScoreSlice> group = new SlicesGroup<>(scores);

        for (ScoreSlice slice : scores.slices().slices()) {
            group.group.put(slice.clop(), slice.slices());
        }

        return group;
    }

    public final ScoreSet scores;

    public SlicesGroup(ScoreSet scores) {
        this.scores = scores;
    }

    @Override
    public PopCount pop() {
        return scores.pop();
    }

    @Override
    public Player player() {
        return scores.player();
    }

    @Override
    public boolean opening() {
        return scores.opening();
    }

    public void close() {
        group.values().forEach(Slices::close);
    }
}
