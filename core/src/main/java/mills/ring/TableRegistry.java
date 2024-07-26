package mills.ring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.01.21
 * Time: 10:52
 */
public class TableRegistry {

    public static final AtomicInteger MAX = new AtomicInteger();

    private final ConcurrentNavigableMap<List<RingEntry>, IndexedEntryTable> fragMap = new ConcurrentSkipListMap<>(Entries.BY_SIZE);
    private final List<IndexedEntryTable> fragments = new ArrayList<>();

    public int count() {
        return fragments.size();
    }

    public int size() {
        return MAX_INDEX + fragments.size();
    }

    public IndexedEntryTable getTable(int index) {
        if (index < 0)
            return IndexedEntryTable.of();

        if (index < MAX_INDEX)
            return Entries.entry(index).singleton;

        return fragments.get(index - MAX_INDEX);
    }

    public IndexedEntryTable getTable(List<RingEntry> entries) {

        if (entries instanceof IndexedEntryTable) {
            int index = ((IndexedEntryTable) entries).getIndex();
            IndexedEntryTable indexed = getTable(index);
            if (indexed == entries)
                return indexed;
        }

        int size = entries.size();

        if (size == 0)
            return IndexedEntryTable.of();

        if (size == 1) {
            return entries.getFirst().singleton;
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

        assert table.size()>1;

        fragMap.put(table, table);

        if(index>MAX.get())
            MAX.updateAndGet(i -> Math.max(i, index));

        return table;
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

        return IndexedEntryTables.of(index, this::getTable);
    }

    public String toString() {
        return String.format("T[%d]", fragments.size());
    }

    public void stat() {
        int count = 0;
        int len = 0;
        for (IndexedEntryTable table : fragMap.values()) {
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
