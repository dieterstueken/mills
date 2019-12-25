package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Positions;
import mills.score.Score;

import java.util.HashMap;
import java.util.List;
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

    public SlicesGroup(PopCount pop, Player player, List<? extends Slices<Slice>> slices) {
        super(pop, player, new HashMap<>());
        for (Slices<Slice> slice : slices) {
            group.put(Clops.of(slice), slice);
        }
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

    public ScoredPosition position(long i201, Player player) {
        boolean inverted = player!=this.player();
        long j201 = inverted ? Positions.inverted(i201) : i201;
        Clops clops = Positions.clops(j201);
        return group.get(clops).scores.position(i201, player);
    }
}
