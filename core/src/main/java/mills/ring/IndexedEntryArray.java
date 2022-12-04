package mills.ring;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.21
 * Time: 20:18
 */
abstract class IndexedEntryArray extends EntryArray implements IndexedEntryTable {

    final int index;

    public int getIndex() {
        return index;
    }

    IndexedEntryArray(short[] indices, int index) {
        super(indices);
        this.index = index;
    }

    static IndexedEntryArray of(List<RingEntry> entries, int key) {
        return of(indices(entries), key);
    }

    static IndexedEntryArray of(short[] indices, int key) {
        return new IndexedEntryArray(indices, key) {
            @Override
            protected RingEntry entry(final int index) {
                return RingEntry.of(index);
            }
        };
    }

    private static short[] indices(List<RingEntry> entries) {
        if(entries instanceof EntryArray)
            return ((EntryArray)entries).indices;

        int size = entries.size();
        short[] indices = new short[size];
        for(int i=0; i<size; ++i)
            indices[i] = entries.get(i).index;

        assert isOrdered(indices) : "index mismatch";
        return indices;
    }
}
