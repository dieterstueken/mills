package mills.score.opening;

import mills.index.PosIndex;
import mills.position.Positions;
import mills.util.AbstractRandomList;
import mills.util.QueueActor;

import java.util.BitSet;
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

    final BitSet bits;

    public OpeningMap(OpeningLayer layer, PosIndex index) {
        this.layer = layer;
        this.index = index;
        final int range = index.range();
        this.bits = new BitSet(range+1);
    }

    public void stat() {
        final int range = index.range();
        final int cardinality = bits.cardinality();
        if(range==cardinality)
            System.out.format("t: %s %,13d\n", layer, range);
        else {
            double r = Math.log((double)range/cardinality)/Math.log(2);
            System.out.format("t: %s %,13d %,13d %.1f\n", layer, range, cardinality, r);
        }
    }

    public void set(int index) {
        bits.set(index);
    }

    private void put(long i201) {
        assert Positions.clops(i201).equals(layer);

        int posIndex = index.posIndex(i201);
        if(posIndex<0)
            throw new IllegalArgumentException("position not found");

        bits.set(posIndex);
    }

    public Target openTarget() {
        return new Target();
    }

    public class Target implements LongConsumer, AutoCloseable {

        static final int CHUNK = Short.MAX_VALUE;

        final List<QueueActor<OpeningMap>> chunks;

        Target() {
            int n = (index.range()+CHUNK-1)/CHUNK;
            chunks = AbstractRandomList.virtual(n, i -> QueueActor.of(OpeningMap.this)).copyOf();
            bits.set(index.range());
        }

        @Override
        public void accept(long i201) {
            int posIndex = index.posIndex(i201);

            if(posIndex<0)
                throw new IllegalArgumentException("position not found");

            int k = posIndex/CHUNK;
            chunks.get(k).submit(map->map.set(posIndex));
        }

        public OpeningMap map() {
            return OpeningMap.this;
        }

        @Override
        public void close() {
            chunks.forEach(QueueActor::close);
            bits.clear(index.range());
            stat();
        }
    }
}
