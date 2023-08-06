package mills.score.opening;

import mills.bits.Clops;
import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.BitSet;
import java.util.function.LongPredicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.08.23
 * Time: 16:12
 */
public class OpeningSet extends OpeningMap {

    final BitSet bits;

    OpeningSet(final PosIndex index, final int turn) {
        super(index, turn);

        this.bits = new BitSet(index.range());
    }

    public static OpeningSet open(IndexProvider provider, int turn, Clops clops) {
        assert placed(turn).sub(clops.clop())!=null;
        PosIndex index = provider.build(clops);
        return new OpeningSet(index, turn);
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

    public OpeningMap complete() {
        if(isComplete())
            return OpeningMap.complete(index, turn);

        return this;
    }

    public boolean isComplete() {
        return bits.previousClearBit(range()-1) < 0;
    }

    public boolean isEmpty() {
        return bits.previousSetBit(range()-1) < 0;
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
