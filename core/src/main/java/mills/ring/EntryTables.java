package mills.ring;

import mills.util.AbstractListSet;
import mills.util.AbstractRandomList;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/21/14
 * Time: 8:49 PM
 */
public class EntryTables {

    static final short OFFSET = RingEntry.MAX_INDEX+1;
    static final int MAX_VALUE = 0xffff - OFFSET - 2;

    private final List<IndexedEntryArray> tables = new ArrayList<>();

    // must never be resized
    private final List<Map<List<RingEntry>, IndexedEntryArray>> maps = AbstractRandomList.preset(RingEntry.MAX_INDEX-2, null);

    // generated maps so far
    private Map<Short, Map<List<RingEntry>, IndexedEntryArray>> metamap = new ConcurrentSkipListMap<>();

    public EntryTables() {
        // prefetch some maps needed anyway
        for(int i=2; i<128; ++i)
            map(i);
    }

    /**
     * Public normalisation.
     * @param list to normalize.
     * @return a normalized list.
     */
    public IndexedEntryTable table(List<RingEntry> list) {

        if(list==null)
            return null;

        int size = list.size();
        if(size==0)
            return IndexedEntryTable.of();

        if(size==1)
            return list.get(0).singleton;

        if(Entries.TABLE.equals(list))
            return Entries.TABLE;

        return getEntry(list);
    }

    private Map<List<RingEntry>, IndexedEntryArray> map(int size) {
        if(size<2)
            throw new IndexOutOfBoundsException("index < 2");
        
        int index = size-2;

        // try map array first
        Map<List<RingEntry>, IndexedEntryArray> map = maps.get(index);

        // generate on demand if still missing
        if(map==null) {
            map = metamap.computeIfAbsent((short)index, i->new ConcurrentSkipListMap<>(Entries.BY_ORDER));
            maps.set(index, map);
        }

        return map;
    }

    private IndexedEntryTable getEntry(List<RingEntry> list) {
        try {
            return _getEntry(list);
        } catch(RuntimeException error) {
            return _getEntry(list);
        }
    }

    private IndexedEntryTable _getEntry(List<RingEntry> list) {

        if(list==null)
            return null;

        if(list instanceof IndexedEntryTable entry) {

            int index = entry.getIndex();
            if(entry == get(index))
                return entry;
        }

        int size = list.size();

        // lookup (no keyed entry of size<2)
        Map<List<RingEntry>, IndexedEntryArray> map = map(size);

        IndexedEntryArray entry = map.get(list);
        if(entry!=null)
            return entry;

        synchronized(map) {

            // double check
            entry = map.get(list);
            if(entry==null) {
                entry = createEntry(list);
                map.put(entry, entry);
            }
        }

        return entry;
    }

    private IndexedEntryArray createEntry(List<RingEntry> list) {

        if(list instanceof EntryArray) {
            short[] indices = ((EntryArray)list).indices;
            return keyedEntry(indices);
        }

        short[] indices = new short[list.size()];
        short l=-1;
        boolean ordered = true;
        for(int i=0; i<indices.length; ++i) {
            short k = list.get(i).index;
            indices[i] = k;
            ordered &= k>l;
            l=k;
        }

        if(!ordered)
            Arrays.sort(indices);

        return keyedEntry(indices);
    }

    /**
     * Very short synchronized block to generate unique key and table entry.
     * The index array is already created.
     * @param indices of RingEntrys
     * @return a registered KeyedEntry.
     */
    private synchronized IndexedEntryArray keyedEntry(short[] indices) {
        int key = tables.size() + OFFSET;

        if(key>MAX_VALUE)
            throw new IndexOutOfBoundsException("too many entries");

        IndexedEntryArray entry = new IndexedEntryArray(indices, (short) key);
        tables.add(entry);

        return entry;
    }

    public short key(List<RingEntry> list) {
        if(list==null)
            return -2;

        IndexedEntryTable indexed = getEntry(list);
        return (short) indexed.getIndex();
    }

    int size() {
        return tables.size() + OFFSET;
    }

    /**
     * Public lookup of normalized table by index.
     * @param index previously generated.
     * @return a normalized EntryTable.
     */
    public EntryTable get(int index) {

        if(index<-1)
            return null;

        if(index == -1)
            return EntryTable.of();

        if(index < RingEntry.MAX_INDEX)
            return Entries.of(index).singleton;

        if(index == RingEntry.MAX_INDEX)
            return Entries.TABLE;

        index -= OFFSET;
        return index<tables.size() ? tables.get(index) : null;
    }

    public List<EntryTable> register(Collection<? extends List<RingEntry>> s1) {
        if(s1==null)
            return null;

        int size = s1.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1) {
            EntryTable table = EntryTables.this.table(s1.iterator().next());
            return AbstractListSet.singleton(table);
        }

        List<EntryTable> table = allocate(size);

        if(s1 instanceof List) {
            List<? extends List<RingEntry>> l1 = (List)s1;
            for (int i = 0; i < size; ++i) {
                List<RingEntry> list = l1.get(i);
                table.set(i, table(list));
            }
        } else {
            int i = 0;
            for (List<RingEntry> list : s1) {
                table.set(i, table(list));
                ++i;
            }

            if (i != size)
                throw new IllegalStateException("size does not match");
        }
        
        return table;
    }

    public List<EntryTable> allocate(int size) {
        return allocate(size, null);
    }

    public List<EntryTable> allocate(int size, EntryTable defaultValue) {

        short[] indexes = new short[size];
        Arrays.fill(indexes, key(defaultValue));

        return new AbstractRandomList<>() {

            @Override
            public int size() {
                return indexes.length;
            }

            @Override
            public EntryTable get(int index) {
                int key = indexes[index];
                return EntryTables.this.get(key);
            }

            @Override
            public EntryTable set(int index, EntryTable table) {
                EntryTable prev = get(index);
                short key = key(table);
                indexes[index] = key;
                return prev;
            }
        };
    }

    public int count() {
        return tables.size();
    }

    public void stat(PrintStream out) {
        out.format("total: %d/%d\n", metamap.size(), tables.size());
        metamap.forEach((key, value) -> out.format("%2d %3d\n", key + 2, value.size()));
    }
}
