package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.BitSet;
import java.util.function.LongPredicate;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  05.03.2021 08:53
 * modified by: $
 * modified on: $
 */
public class OpeningMap extends OpeningLayer {

    final PosIndex index;

    final BitSet bits;

    OpeningMap(PosIndex index, int turn, Clops clops) {
        super(turn, clops);
        this.index = index;
        this.bits = new BitSet(index.range());
    }

    public static OpeningMap open(IndexProvider provider, int turn, Clops clops) {
        PosIndex index = provider.build(clops);
        return new OpeningMap(index, turn, clops);
    }

    void set(int pos) {
        bits.set(pos);
    }

    public boolean get(long i201) {
        int pos = index.posIndex(i201);
        return bits.get(pos);
    }

    void propagate(LongPredicate source) {
        MapProcessor processor = new MapProcessor(this, source);
        processor.run();
    }

    OpeningLayer reduce() {
        if(isComplete())
            return new OpeningLayer(turn, clops);

        return this;
    }

    public boolean isComplete() {
        return bits.previousClearBit(range()-1) < 0;
    }

    public boolean isEmpty() {
        return bits.previousSetBit(range()-1) < 0;
    }

    public int range() {
        return index.range();
    }

    public String toString() {
        int range = index.range();
        int cardinality = bits.cardinality();
        double db = 10*Math.log((double)range/cardinality)/Math.log(10);

        String format = "O%d%c%d%dc%d%d %,13d %,13d (%.1f)";

        if(cardinality==0)
            format = "O%d%c%d%dc%d%d %,13d 0";
        else if(range==cardinality)
            format = "O%d%c%d%dc%d%d %,13d complete";

        return String.format(format, turn/2,
                player().key(),
                pop().nb, pop().nw,
                clop().nb, clop().nw,
                range, cardinality, db);
    }
}
