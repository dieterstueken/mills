package mills.score.opening;

import mills.index.IndexProcessor;
import mills.index.PosIndex;

import java.util.BitSet;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.08.22
 * Time: 15:29
 */
public class BitMap {

    static final int SIZE = 16*4098;

    final BitSet bits;

    final int start;
    final int size;

    int cardinality = -1;

    BitMap(int start, int size) {
        bits = new BitSet(size);
        this.start = start;
        this.size = size;
    }

    public void set(int index) {
        bits.set(index);
        cardinality = -1;
    }

    public int cardinality() {
        int cardinality = this.cardinality;
        if(cardinality<0)
            this.cardinality = cardinality = bits.cardinality();
        return cardinality;
    }

    public void process(PosIndex index, IndexProcessor processor) {
        int cardinality = cardinality();
        if(cardinality == size)
            index.process(processor, start, start+size);
        else if(10L*cardinality>9L*size)
            index.process(filter(processor), start, start+size);
        else
            process(index, processor, start, start+size);
    }

    private void process(PosIndex index, IndexProcessor processor, int start, int end) {

         while(start<end) {
             int next = bits.nextSetBit(start);
             if(next<0 || next>=end)
                 break;

             int stop = bits.nextClearBit(next+1);
             if(stop<0 || stop>end)
                 stop = end;

             index.process(processor, next, stop);
             start = stop;
         }
     }

    private IndexProcessor filter(IndexProcessor other) {
        return (int posIndex, long i201) -> {
            int i = posIndex-start;
            if(i>=0 && bits.get(i))
                other.process(posIndex, i201);
        };
    }

    public static List<BitMap> list(int size) {
        int count = (size + SIZE - 1) / SIZE;
        return IntStream.range(0, count)
                .mapToObj(i -> new BitMap(i*SIZE, i*SIZE<size ? SIZE : size%SIZE))
                .toList();
    }
}
