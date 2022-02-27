package mills.score.attic.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Position;
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
public class PlopSets implements Moves.Process {

    final Plop plop;

    final IndexProvider indexes;

    Map<Clops, PlopSet> plops = new ConcurrentHashMap<>();

    PlopSets(IndexProvider indexes, Plop plop) {
        this.plop = plop;
        this.indexes = indexes;
    }

    protected PlopSets(PlopLayer parent) {
        this.plop = parent.plop;
        this.indexes = parent.indexes;
    }

    private PlopSet newPlops(Clops clops) {

        assert plop.pop.sub(clops.pop()) !=null;

        PosIndex index = index(clops);
        return new PlopSet(plop, index);
    }

    PosIndex index(Clops clops) {
        return indexes.build(clops);
    }

    public Player player() {
        return plop.player();
    }

    PlopSet plops(Clops clops) {
        PlopSet ps = plops.get(clops);
        if(ps==null) {
            synchronized (plops) {
                ps = plops.computeIfAbsent(clops, this::newPlops);
            }
        }
        return ps;
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

        // may be missing
        if(ps==null)
            return false;

        int index = ps.index.posIndex(i201);
        return ps.get(index);
    }

    @Override
    public boolean process(int stay, int move, int mask) {
        // apply move
        move ^= mask;
        Player player = player();
        long i201 = Stones.i201(stay, move, player);
        try {
            return lookup(i201);
        } catch (Throwable err) {
            Position pos = Position.of(i201);
            System.out.println(pos);
            lookup(i201);
            throw err;
        }
    }

    void forEach(Consumer<? super PlopSet> process) {
        plops.values().parallelStream().forEach(process);
    }
}
