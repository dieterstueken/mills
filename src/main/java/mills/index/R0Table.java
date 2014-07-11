package mills.index;

import mills.position.Positions;
import mills.ring.EntryTable;
import mills.util.IndexTable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.ToIntFunction;

import static mills.position.Positions.i0;
import static mills.position.Positions.i1;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 12:25
 */
public class R0Table extends IndexedMap<EntryTable> {

    int idx01(long i201) {
        final short i0 = i0(i201);
        final short i1 = i1(i201);
        return idx01(i0, i1);
    }

    // return relative index of (i0,i1)
    int idx01(short i0, short i1) {

        // lookup position of i0
        final int pos = keys.findIndex(i0);
        if(pos==-1)
            return -1;
        if(pos<-1)
            return -baseIndex(-2 - pos);

        // get relative indexes
        final int idx0 = baseIndex(pos);
        final int idx1 = values.get(pos).findIndex(i1);

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

        assert idx01>=0;

        int pos = it.upperBound(idx01);

        int i0 = keys.ringIndex(pos);
        idx01 -= baseIndex(pos);

        int i1 = values.get(pos).ringIndex(idx01);

        return Positions.i201(i2, i0, i1) | Positions.NORMALIZED;
    }

    boolean process(final int base, final short i2, final IndexProcessor processor, final int start, final int end) {

        int i = start>base ? it.upperBound(start-base) : 0;
        boolean any = false;

        for(; i< it.size(); i++) {
            if(!foreach(base + baseIndex(i), i2, keys.ringIndex(i), values.get(i), processor, start, end))
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

    R0Table(EntryTable t0, List<EntryTable> t1, IndexTable it) {
        super(t0, t1, it);
    }

    public static final R0Table EMPTY = new R0Table(
            EntryTable.EMPTY, Collections.<EntryTable>emptyList(), IndexTable.EMPTY
    );

    public static R0Table of(EntryTable r0, List<EntryTable> t1, IndexTable it) {
        assert r0.size() == it.size();
        assert r0.size() == t1.size();

        if(r0.isEmpty())
            return EMPTY;

        return new R0Table(r0, t1, it);
    }

    public static final ToIntFunction<Collection<?>> SIZE_OF = Collection::size;

    public static R0Table of(EntryTable r0, List<EntryTable> t1) {
        return of(r0, t1, IndexTable.sum(t1, SIZE_OF));
    }
}
