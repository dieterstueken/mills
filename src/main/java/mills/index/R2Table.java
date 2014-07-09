package mills.index;

import com.google.common.collect.ImmutableList;
import mills.position.Positions;
import mills.ring.EntryTable;
import mills.util.IndexTable;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.07.2014 11:48
 * modified by: $Author$
 * modified on: $Date$
 */
public class R2Table {

    final IndexTable it;

    final EntryTable t2;

    final List<R0Table> t0;

    public int size() {
        final int t = it.size()-1;
        return t<0 ? 0 : it.getIndex(t) + t0.get(t).size();
    }

    public List<R0Table> entries() {
        return t0;
    }

    public int posIndex(long i201) {
        assert Positions.normalized(i201);

        final short i2 = Positions.i2(i201);

        // lookup position of i2
        final int pos = t2.findIndex(i2);
        if(pos==-1)
            return -1;
        if(pos<-1)
            return -it.getIndex(-2-pos);

        R0Table r0 = t0.get(pos);
        int posIndex = r0.idx01(i201);

        int index = it.get(pos);    // base index

        // if missing return lower bound by negative index
        if(posIndex<0)
            posIndex -= index;
        else
            posIndex += index;

        return posIndex;
    }

    long i201(int posIndex) {

        final int pos = it.lowerBound(posIndex);
        R0Table r0 = t0.get(pos);
        short i2 = t2.ringIndex(pos);
        int index = it.get(pos);

        return r0.i201(i2, posIndex-index);
    }

    public IndexProcessor process(IndexProcessor processor, int start, int end) {

        for(int pos = start>0 ? it.lowerBound(start) : 0;
            pos<t0.size(); ++pos) {
            R0Table r0 = t0.get(pos);
            short i2 = t2.ringIndex(pos);
            int index = it.get(pos);

            if(!r0.process(index, i2, processor, start, end))
                break;
        }

        return processor;
    }

    R2Table(IndexTable it, EntryTable t2, List<R0Table> t0) {
        this.it = it;
        this.t2 = t2;
        this.t0 = t0;
    }

    static final R2Table EMPTY = new R2Table(IndexTable.EMPTY, EntryTable.EMPTY, ImmutableList.of());

    public static R2Table of(IndexTable it, EntryTable t2, List<R0Table> t0) {
        int size = it.size();

        assert size == t2.size();
        assert size == t0.size();

        if(size==0)
            return EMPTY;

        return new R2Table(it, t2, ImmutableList.copyOf(t0));
    }

    public static R2Table of(EntryTable t2, List<R0Table> t0) {
        return of(IndexTable.build(t0, R0Table.INDEXER), t2, t0);
    }
}
