package mills.score.opening;

import mills.index.IndexProvider;
import mills.index.PosIndex;

import java.util.List;
import java.util.function.LongConsumer;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  05.03.2021 08:53
 * modified by: $
 * modified on: $
 */
public class OpeningMap {

    final OpeningLayer layer;

    final PosIndex index;

    final List<BitMap> bits;

    OpeningMap(PosIndex index, OpeningLayer layer) {
        this.layer = layer;
        this.index = index;

        this.bits = BitMap.list(index.range());
    }

    public static OpeningMap open(IndexProvider provider, OpeningLayer layer) {
        PosIndex index = provider.build(layer.clops);
        return new OpeningMap(index, layer);
    }

    public void set(int pos) {
        bits.get(pos/BitMap.SIZE).set(pos%BitMap.SIZE);
    }

    public int cardinality() {
        return bits.stream().mapToInt(BitMap::cardinality).sum();
    }

    public void stat() {
        final int range = index.range();
        final int cardinality = cardinality();
        if(range==cardinality)
            System.out.format("t: %s %,13d\n", layer, range);
        else {
            double r = Math.log((double)range/cardinality)/Math.log(2);
            System.out.format("t: %s %,13d %,13d %.1f\n", layer, range, cardinality, r);
        }
    }

    public MapTarget openTarget() {
        return new MapTarget(this);
    }

    void propagate(LongConsumer target) {
        bits.parallelStream().forEach(bits->process(bits, target));
    }
    
    void process(BitMap bits, LongConsumer target) {
        MapProcessor processor = new MapProcessor(layer, target);
        bits.process(index, processor);
    }
}
