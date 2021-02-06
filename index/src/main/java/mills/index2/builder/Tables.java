package mills.index2.builder;

import mills.ring.*;
import mills.util.AbstractRandomArray;

import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.01.21
 * Time: 10:52
 */
public class Tables {

    private final ConcurrentNavigableMap<List<RingEntry>, IndexedEntryTable> fragMap = new ConcurrentSkipListMap<>(Entries.BY_SIZE);
    private final List<IndexedEntryTable> fragments = new ArrayList<>();

    public int count() {
        return fragments.size();
    }

    public int size() {
        return MAX_INDEX + fragments.size();
    }

    public IndexedEntryTable get(int index) {
        if (index < 0)
            return EntryTable.EMPTY;

        if (index < MAX_INDEX)
            return Entries.of(index).singleton;

        return fragments.get(index - MAX_INDEX);
    }

    public IndexedEntryTable get(List<RingEntry> entries) {

        if (entries instanceof IndexedEntryTable) {
            int index = ((IndexedEntryTable) entries).getIndex();
            IndexedEntryTable indexed = get(index);
            if (indexed == entries)
                return indexed;
        }

        int size = entries.size();

        if (size == 0)
            return EntryTable.EMPTY;

        if (size == 1) {
            return entries.get(0).singleton;
        }

        IndexedEntryTable table = fragMap.get(entries);
        if (table == null)
            table = register(entries);

        return table;
    }

    private synchronized IndexedEntryTable register(List<RingEntry> entries) {
        IndexedEntryTable table = fragMap.get(entries);
        if (table != null)
            return table;

        int index = fragments.size() + MAX_INDEX;
        table = IndexedEntryTable.of(entries, index);
        fragments.add(table);
        fragMap.put(table, table);

        return table;
    }

    public List<IndexedEntryTable> tablesOf(Collection<? extends List<RingEntry>> tables) {
        return tablesOf(List.copyOf(tables));
    }

    public List<IndexedEntryTable> tablesOf(List<? extends List<RingEntry>> tables) {
        int size = tables.size();

        if(size==0)
            return List.of();

        if(size==1)
            return List.of(register(tables.get(0)));

        short[] index = new short[size];

        for(int i=0; i<tables.size(); ++i) {
            List<RingEntry> entries = tables.get(i);
            IndexedEntryTable table = register(entries);
            index[i] = (short) table.getIndex();
        }

        return new AbstractRandomArray<>(size) {

            @Override
            public IndexedEntryTable get(int i) {
                return Tables.this.get(index[i]);
            }
        };
    }

    public String toString() {
        return String.format("T[%d]", fragments.size());
    }
}
