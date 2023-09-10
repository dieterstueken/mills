package mills.score.opening;

import mills.bits.Clops;
import mills.bits.IClops;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.BitSet;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.08.23
 * Time: 18:11
 */
public class OpeningMap extends OpeningLayer {

    final PosIndex index;

    // null indicates all positions reachable.
    BitSet bits;

    OpeningMap(final PosIndex index, final int turn, boolean complete) {
        super(turn, index.clops());
        this.index = index;
        this.bits = complete ? null : new BitSet(0);
    }

    /**
     * @param provider
     * @param turn
     * @param clops
     * @return an empty bitset with all bits cleared.
     */
    public static OpeningMap open(IndexProvider provider, int turn, IClops clops) {
        PosIndex index = provider.build(clops);
        if(index==null)
            throw new IllegalArgumentException("no indes for: " + clops);
        return new OpeningMap(index, turn, false);
    }

    public static OpeningMap complete(IndexProvider provider, int turn, IClops clops) {
        PosIndex index = provider.build(clops);
        return new OpeningMap(index, turn, true);
    }

    public int range() {
        return index.range();
    }

    public static OpeningMap empty() {
        return new OpeningMap(PosIndex.EMPTY, 0, true);
    }

    public void set(int pos) {
        BitSet bits = this.bits;
        if(bits!=null) {
            if(bits.isEmpty()) {
                // first hit
                synchronized (bits) {
                    // enforce a resize
                    bits.set(range());
                    bits.clear(range());
                }
            }
            bits.set(pos);
        }
    }

    public boolean get(int posIndex) {
        BitSet bits = this.bits;
        return bits==null || bits.get(posIndex);
    }

    public boolean isComplete() {
        BitSet bits = this.bits;
        if(bits!=null) {
            if (bits.isEmpty() || bits.previousClearBit(range() - 1) >= 0)
                return false;
            else
                this.bits = null;
        }

        return true;
    }

    public boolean isEmpty() {
        return bits!=null && bits.previousSetBit(range()-1) < 0;
    }

    public OpeningMap complete() {
        bits = null;
        return this;
    }

    Stream<MapActor> openProcessors(OpeningMaps target) {

        boolean isComplete = isComplete();
        Clops next = nextClops();

        List<Clops> list = clopsStream().toList();

        List<MapActor> processors =  list.stream().map(clops -> {
            OpeningMap map = target.openMap(clops);
            if(isComplete && next.equals(map))
                map.complete();

            return MapActor.open(map);
        }).toList();

        return processors.stream();
    }

    public String toString() {
        int range = index.range();
        int cardinality = bits==null ? range() : bits.cardinality();
        double db = 10*Math.log((double)range/cardinality)/Math.log(10);

        String format = "O%d%c%d%dc%d%d-%d%d %,13d %,d (%.1f)";

        if(isComplete())
            format = "O%d%c%d%dc%d%d-%d%d %,13d complete";
        //else
        //    format = "O%d%c%d%dc%d%d %,13d";

        PopCount mst = placed().sub(pop()).swap();

        return String.format(format, turn/2,
                player().key(),
                pop().nb, pop().nw,
                clop().nb, clop().nw,
                mst.nb, mst.nw,
                range, cardinality, db);
    }
}
