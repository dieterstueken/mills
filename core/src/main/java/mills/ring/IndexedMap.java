package mills.ring;

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

    protected final IndexTable it;

    public int range() {
        return it.range();
    }

    public IndexedMap(EntryTable keys, List<T> values, IndexTable it) {
        super(keys, values);

        this.it = it;
        assert it.size() == size();
    }

    public IndexedMap(EntryTable keys, List<T> values, final ToIntFunction<? super T> indexer) {
        super(keys, values);
        this.it = IndexTable.sum(values, indexer);
    }
}
