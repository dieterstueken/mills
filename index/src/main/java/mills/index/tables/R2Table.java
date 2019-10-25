package mills.index.tables;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.ring.EntryTable;
import mills.ring.IndexedMap;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.07.2014 11:48
 * modified by: $Author$
 * modified on: $Date$
 */
public abstract class R2Table extends IndexedMap<R0Table> implements PosIndex {

    public long normalize(long i201) {
        return normalizer().normalize(i201);
    }

    public int posIndex(long i201) {

        if(!Positions.normalized(i201))
            i201 = normalize(i201);
        else
            assert i201 == normalize(i201);

        final short i2 = Positions.i2(i201);

        // lookup position of i2
        final int pos = keys.findIndex(i2);
        if(pos==-1)
            return -1;
        if(pos<-1)
            return -baseIndex(-2 - pos);

        R0Table r0 = values.get(pos);
        int posIndex = r0.idx01(i201);

        int baseIndex = baseIndex(pos);    // base index

        // if missing return lower bound by negative index
        if(posIndex<0)
            posIndex -= baseIndex;
        else
            posIndex += baseIndex;

        return posIndex;
    }

    public long i201(int posIndex) {

        final int pos = indexOf(posIndex);
        R0Table r0 = values.get(pos);
        short i2 = keys.ringIndex(pos);
        int index = baseIndex(pos);

        return r0.i201(i2, posIndex-index);
    }

    public IndexProcessor process(IndexProcessor processor, int start, int end) {

        for(int pos = start>0 ? indexOf(start) : 0;
            pos< values.size(); ++pos) {
            R0Table r0 = values.get(pos);
            short i2 = keys.ringIndex(pos);
            int baseIndex = baseIndex(pos);

            if(!r0.process(baseIndex, i2, processor, start, end))
                break;
        }

        return processor;
    }

    @Override
    public int n20() {
        return values.size();
    }

    final PopCount pop;

    public final PopCount pop() {
        return pop;
    }

    R2Table(final PopCount pop, EntryTable t2, List<R0Table> t0) {
        super(t2, t0, R0Table::range);

        this.pop = pop;
    }
}
