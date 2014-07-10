package mills.index;

import mills.util.Indexer;

import static mills.position.Positions.i0;
import static mills.position.Positions.i1;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.07.12
 * Time: 18:00
 */
public class R2Entry {

    // relative index
    final int index;

    final short i2;

    final R0Table t0;

    public R2Entry(int index, short i2, R0Table t0) {
        this.index = index;
        this.i2 = i2;
        this.t0 = t0;
    }

    public int size() {
        return index + t0.range();
    }

    public int count() {
        return t0.range();
    }

    int posIndex(long n201) {
        final short i0 = i0(n201);
        final short i1 = i1(n201);

        int posIndex = t0.idx01(i0, i1);

        // if missing return lower bound by negative index

        if(posIndex<0)
            posIndex -= index;
        else
            posIndex += index;

        return posIndex;
    }

    long i201(int posIndex) {
        return t0.i201(i2, posIndex-index);
    }

    boolean process(IndexProcessor processor, int start, int end) {
      return t0.process(index, i2, processor, start, end);
    }

    static final Indexer<R2Entry> INDEX = new Indexer<R2Entry>() {

        @Override
        public int index(R2Entry element) {
            return element.index;
        }
    };

    static final Indexer<R2Entry> R2 = new Indexer<R2Entry>() {

        @Override
        public int index(R2Entry element) {
            return element.i2;
        }
    };
}
