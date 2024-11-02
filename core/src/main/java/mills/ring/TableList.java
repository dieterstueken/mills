package mills.ring;

import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.24
 * Time: 16:36
 */
public class TableList extends AbstractRandomList<IndexedEntryTable> {

    private final ArrayList<IndexedEntryTable> tableList = new ArrayList<>();

    private final AtomicInteger counter = new AtomicInteger(0);

    public int count() {
        return counter.get();
    }

    @Override
    public int size() {
        return MAX_INDEX + 1 + count();
    }

    /**
     * Lookup an IndexedEntryTable by its index.
     * @param index index of the element to return
     * @return the IndexedEntryTable.
     */
    @Override
    public IndexedEntryTable get(int index) {

        // return an empty table
        if (index < 0)
            return IndexedEntryTable.of();

        // virtual singleton entries.
        if (index < MAX_INDEX)
            return Entries.entry(index).singleton();

        // this returns the full table.
        if (index == MAX_INDEX)
            return Entries.TABLE;

        int pos = index - MAX_INDEX - 1;

        IndexedEntryTable fragment = tableList.get(pos);

        assert fragment!=null && fragment.getIndex() == index;

        return fragment;
    }

    /**
     * Register a new IndexedEntryTable with a unique index.
     * @param entries to register.
     * @return a new IndexedEntryTable.
     */
    IndexedEntryTable register(List<RingEntry> entries) {

        int pos = counter.getAndIncrement();

        // reserve additional capacity (>16) to be able to set up values concurrently.
        int capacity = (pos+0x10)|0x0f;
        if(tableList.size()<capacity)
            ensureCapacity(capacity);

        IndexedEntryTable table = IndexedEntryTable.of(entries, pos + MAX_INDEX + 1);

        tableList.set(pos, table);
        return table;
    }

    private synchronized void ensureCapacity(int size) {
        tableList.ensureCapacity(size);
        while(tableList.size()<size) {
            tableList.add(null);
        }
    }

    public IndexedEntryTables tablesOf(short[] keys) {

        return new IndexedEntryTables() {

            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public IndexedEntryTable get(int index) {
                int key = keys[index];
                return TableList.this.get(key);
            }
        };
    }

    @Override
    public String toString() {
        return String.format("T[%d]", tableList.size());
    }
}
