package mills.ring;

import mills.util.AbstractRandomList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Class TableRegistry has a cache of entry tables.
 * An incoming list of RingEntry elements is converted into an uniq IndexedEntryTable.
 * The generated table can later be retrieved again by its index.
 */
public class TableRegistry extends AbstractRandomList<IndexedEntryTable> {

    private final TableList tableList = new TableList();

    private final Map<List<RingEntry>, IndexedEntryTable> tableMap = new ConcurrentSkipListMap<>(Entries.BY_SIZE);

    public int count() {
        return tableList.count();
    }

    public int size() {
        return tableList.size();
    }

    /**
     * Lookup an IndexedEntryTable by its index.
     * @param index index of the element to return
     * @return the IndexedEntryTable.
     */
    public IndexedEntryTable get(int index) {
        return tableList.get(index);
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

        if (size == 1)
            return entries.getFirst().singleton();

        if(Entries.TABLE.equals(entries))
            return Entries.TABLE;

        // may be it is already an IndexedEntryTable with the expected values.
        // don't trigger a synchronisation yet.
        if(entries instanceof IndexedEntryTable itable) {
            int index = itable.getIndex();
            if(index>0 && index<tableList.size()) {
                IndexedEntryTable jtable = tableList.get(index);
                if(jtable!=null && jtable.equals(itable))
                    return jtable;
            }
        }
        
        // normalize to EntryTable to stabilize any virtual lists.
        entries = EntryTable.of(entries);

        return tableMap.computeIfAbsent(entries, tableList::register);
    }

    public IndexedEntryTables tablesOf(Collection<? extends List<RingEntry>> tables) {
        int size = tables.size();

        if(size==0)
            return IndexedEntryTables.EMPTY;

        if(size==1)
            return IndexedEntryTables.of(getTable(tables.iterator().next()));

        return tablesOf(List.copyOf(tables));
    }

    public IndexedEntryTables tablesOf(List<? extends List<RingEntry>> tables) {
        int size = tables.size();

        if(size==0)
            return IndexedEntryTables.EMPTY;

        if(size==1)
            return IndexedEntryTables.of(getTable(tables.getFirst()));

        short[] keys = new short[size];

        for(int i=0; i<tables.size(); ++i) {
            List<RingEntry> entries = tables.get(i);
            IndexedEntryTable table = getTable(entries);
            keys[i] = (short) table.getIndex();
        }

        IndexedEntryTables indexedTables = tablesOf(keys);

        assert indexedTables.equals(tables);

        return indexedTables;
    }

    public IndexedEntryTables tablesOf(short[] keys) {
        return tableList.tablesOf(keys);
    }

    public String toString() {
        return tableList.toString();
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
