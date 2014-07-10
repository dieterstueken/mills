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

    protected IndexedMap(EntryTable keys, List<T> values, IndexTable it) {
        super(keys, values);

        this.it = it;

        assert it.size() == size();
    }

    protected IndexedMap(EntryTable keys, List<T> values, ToIntFunction<T> count) {
        this(keys, values, IndexTable.build(values, count));
    }

}
