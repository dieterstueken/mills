package mills.ring;

import mills.util.Indexed;

import java.util.List;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  02.02.2021 13:15
 * modified by: $
 * modified on: $
 */
public interface IndexedEntryTable extends EntryTable, Indexed {

    static IndexedEntryTable of() {
        return EmptyTable.EMPTY;
    }

    static IndexedEntryTable of(List<RingEntry> entries, int key) {
        int size = entries.size();

        if(size==0)
            return EmptyTable.EMPTY;

        if(size==1)
            return entries.get(0).singleton;

        return IndexedEntryArray.of(entries, key);
    }
}
