package mills.index3;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.ring.EntryTable;
import mills.ring.IndexedMap;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  20.11.2015 19:55
 * modified by: $Author$
 * modified on: $Date$
 */
public class R2Index implements PosIndex {

    final PopCount pop;

    private final IndexedMap<IndexedMap<EntryTable>> m2;

    public R2Index(IndexedMap<IndexedMap<EntryTable>> m2, PopCount pop) {
        this.m2 = m2;
        this.pop = pop;
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public int range() {
        return m2.range();
    }

    @Override
    public String toString() {
        return String.format("%d %d", m2.size(), range());
    }

    @Override
    public int posIndex(long i201) {
        assert verify(i201) : Positions.position(i201);
        i201 = Positions.normalize(i201);

        final short i2 = Positions.i2(i201);

        int k2 = m2.keySet().findIndex(i2);
        IndexedMap<EntryTable> m0 = m2.values().get(k2);

        final short i0 = Positions.i0(i201);
        int k0 = m0.keySet().findIndex(i0);

        EntryTable m1 = m0.values().get(k0);

        final short i1 = Positions.i0(i201);
        int posIndex = m1.findIndex(i1);

        if(posIndex<0)
            return -1;

        posIndex += m0.baseIndex(k0);
        posIndex += m2.baseIndex(k2);

        return posIndex;
    }

    @Override
    public long i201(int posIndex) {

        int k2 = m2.indexOf(posIndex);
        int i2 = m2.keySet().ringIndex(k2);
        posIndex -= m2.baseIndex(k2);

        IndexedMap<EntryTable> m0 = m2.values().get(k2);
        int k0 = m0.indexOf(posIndex);
        int i0 = m0.keySet().ringIndex(k0);
        posIndex -= m0.baseIndex(k0);

        EntryTable m1 = m0.values().get(k0);
        int i1 = m1.ringIndex(posIndex);

        return Positions.n201(i2, i0, i1);
    }

    @Override
    public IndexProcessor process(IndexProcessor processor, int start, int end) {

        int k2 = m2.indexOf(start);

        while(start<end) {
            IndexedMap<EntryTable> m0 = m2.values().get(k2);

            int base = start-m2.baseIndex(start);
            int k0 = m0.indexOf(base);
            int i2 = m2.keySet().ringIndex(k2);

            for(;k0<m0.size() && start<end;++k0) {
                int i0 = m0.keySet().ringIndex(k0);

                EntryTable t1 = m0.get(k0);
                int k1 = t1.findIndex(base - m0.baseIndex(k0));

                for(;k1<t1.size() && start<end; ++k1, ++start) {
                    int i1 = t1.ringIndex(k1);
                    long n201 = Positions.n201(i2, i0, i1);
                    processor.process(start, n201);
                }
            }
        }

        return processor;
    }
}
