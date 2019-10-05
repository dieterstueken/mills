package mills.index1;

import mills.index.IndexProcessor;
import mills.ring.Entries;
import mills.ring.RingEntry;
import mills.util.Indexer;

import static mills.position.Positions.i0;
import static mills.position.Positions.i1;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 28.07.12
 * Time: 18:00
 */
public class I2Entry extends R2Entry {

    static final Indexer<I2Entry> INDEX = element -> element.index;

    // relative index
    final int index;
    final int base;

    public I2Entry(int index, short i2, R0Table t0) {
        this(index, Entries.of(i2), t0);
    }

    public I2Entry(int index, RingEntry e2, R0Table t0) {
        super(e2, t0);
        this.index = index;
        this.base = index - t0.range();
    }

    public int size() {
        return t0.size();
    }

    public int range() {
        return index;
    }

    public R0Table values() {
        return t0;
    }

    public String toString() {
        return String.format("%d %d %d", index, e2.index, t0.size());
    }

    int posIndex(long n201) {
        final short i0 = i0(n201);
        final short i1 = i1(n201);

        int posIndex = t0.idx01(i0, i1);

        // if missing return lower bound by negative index

        if(posIndex<0)
            posIndex -= base;
        else
            posIndex += base;

        return posIndex;
    }

    long i201(int posIndex) {
        return t0.i201(e2.index, posIndex-base);
    }

    boolean process(IndexProcessor processor, int start, int end) {
      return t0.process(base, e2.index, processor, start, end);
    }

}
