package mills.score.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.stones.Moves;
import mills.stones.Stones;

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
public class PlopSets extends Plop implements Moves.Process {

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
        Clops clops = Clops.of(pop, clop);
        return plops(clops);
    }

    boolean lookup(long i201) {
        PopCount pop = Positions.pop(i201);
        PopCount clop = Positions.clop(i201);
        Clops clops = Clops.of(pop, clop);
        PlopSet ps = plops.get(clops);
        // check for null
        int index = ps.index.posIndex(i201);
        return ps.get(index);
    }

    @Override
    public boolean process(int stay, int move, int mask) {
        // apply move
        move ^= mask;
        Player player = player();
        long i201 = Stones.i201(stay, move, player);
        return lookup(i201);
    }

    void forEach(Consumer<? super PlopSet> process) {
        plops.values().forEach(process);
    }
}
