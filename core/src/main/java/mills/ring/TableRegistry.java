package mills.ring;

import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Class TableRegistry has a cache of entry tables.
 * An incoming list of RingEntry elements is converted into an uniq IndexedEntryTable.
 * The generated table can later be retrieved again by its index.
 */
public class TableRegistry extends AbstractRandomList<IndexedEntryTable> {

    private final AtomicInteger counter = new AtomicInteger(0);
    // the actual size of fragments may increase during synchronisation.
    private int count = 0;

    private final ArrayList<IndexedEntryTable> tables = new ArrayList<>();

    private final Map<List<RingEntry>, IndexedEntryTable> tableMap = new ConcurrentSkipListMap<>(Entries.BY_SIZE);

    public int count() {
        return tableMap.size();
    }

    public int size() {
        return MAX_INDEX + 1 + count;
    }

    /**
     * Lookup an IndexedEntryTable by its index.
     * @param index index of the element to return
     * @return the IndexedEntryTable.
     */
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

        // we should get it after synchronisation.
        if(pos>=count && pos < counter.get())
            synchronize();

        IndexedEntryTable fragment = tables.get(pos);

        assert fragment.getIndex() == index;

        return fragment;
    }

    /**
     * Register a list of entries as an IndexedEntryTable.
     * If the list is already registered, the already registered table is returned.
     * @param entries to register.
     * @return a IndexedEntryTable containing the given entries.
     */
    public IndexedEntryTable getTable(List<RingEntry> entries) {

        // look for predefined tables.
        int size = entries.size();

        if (size == 0)
            return IndexedEntryTable.of();

        if (size == 1) {
            return entries.getFirst().singleton();
        }

        // quick lookup
        IndexedEntryTable table = tableMap.get(entries);

        if (table == null) {
            // normalize to EntryTable to stabilize any virtual lists.
            entries = EntryTable.of(entries);

            table = tableMap.computeIfAbsent(entries, this::register);
        }

        return table;
    }

    /**
     * Register a new IndexedEntryTable with a unique index.
     * @param entries to register.
     * @return a new IndexedEntryTable.
     */
    private IndexedEntryTable register(List<RingEntry> entries) {
        int index = counter.getAndIncrement();
        return IndexedEntryTable.of(entries, index + MAX_INDEX + 1);
    }

    /**
     * Transfer all registered entries from fragMap into the linear lookup table.
     */
    public synchronized void synchronize() {

        // double check.
        if(count==counter.get())
            return;

        if(count!= tables.size())
            throw new IllegalStateException("unexpected fragment size");

        // internal resize
        tables.ensureCapacity(counter.get());

        // add missing entries.
        for (IndexedEntryTable fragment : tableMap.values()) {
            int index = fragment.getIndex() - MAX_INDEX - 1;

            // have to add it.
            if(index>=count) {
                // resize on demand
                while(tables.size()<=index) {
                    tables.add(null);
                }
                // place fragment at its position.
                tables.set(index, fragment);
            }
            // There is a very small change to leave null entries.
            // This happens, if concurrent computeIfAbsent calls register twice but uses only one result.
            // As the unused result never entered the fragMap, it will never be exposed and thus never be queried.
        }

        // publish new fragments.
        count = tables.size();
    }

    public IndexedEntryTables tablesOf(Collection<? extends List<RingEntry>> tables) {
        return tablesOf(List.copyOf(tables));
    }

    public IndexedEntryTables tablesOf(List<? extends List<RingEntry>> tables) {
        int size = tables.size();

        if(size==0)
            return IndexedEntryTables.EMPTY;

        if(size==1)
            return IndexedEntryTables.of(getTable(tables.getFirst()));

        short[] index = new short[size];

        for(int i=0; i<tables.size(); ++i) {
            List<RingEntry> entries = tables.get(i);
            IndexedEntryTable table = getTable(entries);
            index[i] = (short) table.getIndex();
        }

        IndexedEntryTables indexedTables = IndexedEntryTables.of(index, this::get);

        assert indexedTables.equals(tables);

        return indexedTables;
    }

    public String toString() {
        return String.format("T[%d]", tables.size());
    }

    public void stat() {
        int count = 0;
        int len = 0;
        for (IndexedEntryTable table : tableMap.values()) {
            int size = table.size();
            if(size!=len && count>0) {
                System.out.format("%3d: %4d\n", len, count);
                count = 0;
                len = size;
            }
            ++count;
        }

        System.out.format("%3d: %4d\n", len, count);
    }
}
