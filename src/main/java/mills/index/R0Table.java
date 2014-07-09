package mills.index;

import mills.position.Positions;
import mills.ring.EntryTable;
import mills.util.IndexTable;
import mills.util.Indexer;

import java.util.Collections;
import java.util.List;

import static mills.position.Positions.i0;
import static mills.position.Positions.i1;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 12:25
 */
public class R0Table {

    final IndexTable index;

    final EntryTable t0;

    final List<EntryTable> t1;

    public int size() {
        final int t = index.size()-1;
        return t<0 ? 0 : index.getIndex(t) + t1.get(t).size();
    }

    public List<EntryTable> entries() {
        return t1;
    }

    int idx01(long i201) {
        final short i0 = i0(i201);
        final short i1 = i1(i201);
        return idx01(i0, i1);
    }

    // return relative index of (i0,i1)
    int idx01(short i0, short i1) {

        // lookup position of i0
        final int pos = t0.findIndex(i0);
        if(pos==-1)
            return -1;
        if(pos<-1)
            return -index.getIndex(-2-pos);

        // get relative indexes
        final int idx0 = index.getIndex(pos);
        final int idx1 = t1.get(pos).findIndex(i1);

        // if missing return lower bound by negative index
        if(idx1<0)
            return idx1 - idx0;
        else
            return idx1 + idx0;
    }

    /**
     * Build i201 pattern for a relative index and a given i2 entry.
     * @param i2 index to be used.
     * @param idx01 relative index to address (i0, i1) pair.
     * @return i201 pattern
     */
    public long i201(short i2, int idx01) {

        int pos = index.lowerBound(idx01);
        int i0 = t0.ringIndex(pos);
        idx01 -= index.getIndex(pos);

        int i1 = t1.get(pos).ringIndex(idx01);

        return Positions.i201(i2, i0, i1) | Positions.NORMALIZED;
    }

    boolean process(final int base, final short i2, final IndexProcessor processor, final int start, final int end) {

        int i = start>base ? index.lowerBound(start-base) : 0;
        boolean any = false;

        for(; i<index.size(); i++) {
            if(!foreach(base + index.getIndex(i), i2, t0.ringIndex(i), t1.get(i), processor, start, end))
                break;
            any = true;
        }

        return any;
    }

    /////////////////////////////////////////////////////////////////////

    static boolean foreach(int base, short i2, short i0, EntryTable t1, IndexProcessor processor, int start, int end) {
        int i = start>base ? start-base : 0;
        int l = Math.min(t1.size(), end-base);

        if(i>=l) // nothing to do
            return false;

        for(; i<l; ++i) {
            short i1 = t1.ringIndex(i);
            long i201 = Positions.NORMALIZED | Positions.i201(i2, i0, i1);
            processor.process(base + i, i201);
        }

        return true;
    }

    R0Table(IndexTable it, EntryTable t0, List<EntryTable> t1) {
        this.index = it;
        this.t0 = t0;
        this.t1 = t1;
    }

    public static final Indexer<R0Table> INDEXER = new Indexer<R0Table>() {

        @Override
        public int index(R0Table t) {
            return t.size();
        }
    };

    public static final R0Table EMPTY = new R0Table(
            IndexTable.EMPTY,
            EntryTable.EMPTY,
            Collections.<EntryTable>emptyList());

    public static R0Table of(IndexTable it, EntryTable r0, List<EntryTable> t1) {
        assert r0.size() == it.size();
        assert r0.size() == t1.size();

        return new R0Table(it, r0, t1);
    }

    public static R0Table of(EntryTable r0, List<EntryTable> t1) {
        return of(IndexTable.build(t1), r0, t1);
    }
}
