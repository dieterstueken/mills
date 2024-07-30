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
        return EntryTable.empty();
    }

    static IndexedEntryTable of(List<RingEntry> entries, int key) {
        int size = entries.size();

        if(size==0)
            return EntryTable.empty();

        if(size==1)
            return entries.getFirst().singleton;

        return IndexedEntryArray.of(entries, key);
    }
}
