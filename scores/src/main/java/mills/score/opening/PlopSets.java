package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  14.11.2019 12:42
 * modified by: $
 * modified on: $
 */
public class PlopSets extends Plop {

    final IndexProvider indexes;

    Map<Clops, PlopSet> plops = new ConcurrentHashMap<>();

    PlopSets(IndexProvider indexes, int layer) {
        super(layer);
        this.indexes = indexes;
    }

    protected PlopSets(PlopLayer parent) {
        super(parent);
        this.indexes = parent.indexes;
    }

    private PlopSet newPlops(Clops clops) {

        assert plop.sub(clops.pop()) !=null;

        PosIndex index = index(clops);
        return new PlopSet(this, index);
    }

    PosIndex index(Clops clops) {
        return indexes.build(clops);
    }

    public Player player() {
        return plop.sum()%2==0 ? Player.White : Player.Black;
    }

    PlopSet plops(Clops clops) {
        return plops.computeIfAbsent(clops, this::newPlops);
    }

    PlopSet plops(PopCount pop, PopCount clop) {
        Clops clops = Clops.get(pop, clop);
        return plops(clops);
    }

    void process(Consumer<PlopSet> process) {
        plops.values().forEach(process);
    }
}
