package mills.score.generator;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Positions;

import java.util.Map;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:15
 */
public class SlicesGroup<Slices extends ScoreSlices> extends LayerGroup<Slices> {

    public SlicesGroup(PopCount pop, Player player, Map<Clops, Slices> slices) {
        super(pop, player, slices);
    }

    public SlicesGroup(PopCount pop, Player player, Stream<Slices> slices) {
        super(pop, player, slices);
    }

    public int max() {
        int max = 0;
        for (Slices slice : group.values()) {
            max = Math.max(0, slice.max());
        }
        return max;
    }

    public int getScore(long i201) {
        Clops clops = Positions.clops(i201);
        return group.get(clops).getScore(i201);
    }

    public ScoredPosition position(long i201) {
        Clops clops = Positions.clops(i201);
        return group.get(clops).scores().position(i201);
    }

    public ScoredPosition position(long i201, Player player) {
        if(player==this.player)
            return position(i201);

        i201 = Positions.inverted(i201);
        return position(i201).inverted();
    }
}
