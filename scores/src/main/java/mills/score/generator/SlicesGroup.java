package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.score.Score;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:15
 */
public class SlicesGroup<Slice extends ScoreSlice> extends LayerGroup<Slices<Slice>> {

    public SlicesGroup(PopCount pop, Player player, Map<Clops, Slices<Slice>> slices) {
        super(pop, player, slices);
    }

    Stream<? extends Slice> slices() {
        return group.values().stream().flatMap(Slices::stream);
    }

    Stream<? extends Slice> slices(Score score) {
        return slices().filter(slice->slice.hasScores(score));
    }

    public int max() {
        int max = 0;
        for (Slices<? extends Slice> slice : group.values()) {
            max = Math.max(0, slice.max());
        }
        return max;
    }

    public void close() {
        group.values().forEach(Slices::close);
    }
}
