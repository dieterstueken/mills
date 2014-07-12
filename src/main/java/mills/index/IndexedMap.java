package mills.index;

import mills.ring.EntryMap;
import mills.ring.EntryTable;
import mills.util.IndexTable;

import java.util.List;
import java.util.function.ToIntFunction;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  10.07.2014 16:20
 * modified by: $Author$
 * modified on: $Date$
 */
public class IndexedMap<T> extends EntryMap<T> {

    final IndexTable it;

    public int range() {
        int size = it.size();
        return size>0 ? it.get(size-1) : 0;
    }

    public int baseIndex(int pos) {
        return pos==0 ? 0 : it.get(pos-1);
    }

    protected IndexedMap(EntryTable keys, List<T> values, IndexTable it) {
        super(keys, values);

        this.it = it;
        assert it.size() == size();
    }

    protected IndexedMap(EntryTable keys, List<T> values, final ToIntFunction<? super T> indexer) {
        super(keys, values);
        this.it = IndexTable.sum(values, indexer);
    }
}
