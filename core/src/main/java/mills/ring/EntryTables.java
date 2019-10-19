package mills.ring;

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

    static class KeyedEntry extends EntryArray {

        final short key;

        public int key() {
            return key&0xffff;
        }

        KeyedEntry(short[] indices, short key) {
            super(indices);
            this.key = key;
        }
    }

    static final short OFFSET = RingEntry.MAX_INDEX+1;
    static final int MAX_VALUE = Short.MAX_VALUE - OFFSET;

    private final List<KeyedEntry> tables = new ArrayList<>();

    // must never be resized
    private final List<Map<List<RingEntry>, KeyedEntry>> maps = AbstractRandomList.preset(RingEntry.MAX_INDEX-2, null);

    // generated maps so far
    private Map<Short, Map<List<RingEntry>, KeyedEntry>> metamap = new ConcurrentSkipListMap<>();

    /**
     * Public normalisation.
     * @param list to normalize.
     * @return a normalized list.
     */
    public EntryTable table(List<RingEntry> list) {

        int size = list.size();
        if(size==0)
            return EntryTable.EMPTY;

        if(size==1)
            return list.get(0).singleton;

        if(Entries.TABLE.equals(list))
            return Entries.TABLE;

        return getEntry(list);
    }

    private Map<List<RingEntry>, KeyedEntry> map(int size) {
        int index = size-2;

        // try map array first
        Map<List<RingEntry>, KeyedEntry> map = maps.get(index);

        // generate on demand if still missing
        if(map==null) {
            map = metamap.computeIfAbsent((short)index, i->new ConcurrentSkipListMap<>(Entries.BY_ORDER));
            maps.set(index, map);
        }

        return map;
    }

    private KeyedEntry getEntry(List<RingEntry> list) {

        if(list instanceof KeyedEntry) {
            KeyedEntry entry = (KeyedEntry) list;

            // verify key (might have been generated by an other indexer)
            if(entry == tables.get(entry.key()))
                return entry;
        }

        int size = list.size();

        // lookup (no keyed entry of size<2)
        Map<List<RingEntry>, KeyedEntry> map = map(size);

        KeyedEntry entry = map.get(list);
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

    private KeyedEntry createEntry(List<RingEntry> list) {

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
    private synchronized KeyedEntry keyedEntry(short[] indices) {
        int key = tables.size();

        if(key>MAX_VALUE)
            throw new IndexOutOfBoundsException("too many entries");

        KeyedEntry entry = new KeyedEntry(indices, (short) key);
        tables.add(entry);

        return entry;
    }

    public short key(List<RingEntry> list) {
        int size = list.size();
        if(size==0)
            return -1;

        if(size==1)
            return list.get(0).index;

        if(Entries.TABLE.equals(list))
            return RingEntry.MAX_INDEX;

        KeyedEntry entry = getEntry(list);

        return (short)(entry.key() + OFFSET);
    }

    /**
     * Public lookup of normalized table by index.
     * @param index previously generated.
     * @return a normalized EntryTable.
     */
    public EntryTable get(int index) {

        if(index == -1)
            return EntryTable.EMPTY;

        if(index < RingEntry.MAX_INDEX)
            return Entries.of(index).singleton;

        if(index == RingEntry.MAX_INDEX)
            return Entries.TABLE;

        return tables.get(index-OFFSET);
    }

    public List<EntryTable> register(Collection<? extends List<RingEntry>> s1) {
        int size = s1.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1) {
            EntryTable table = EntryTables.this.table(s1.iterator().next());
            return table.singleton();
        }

        short[] indexes = new short[size];

        if(s1 instanceof List) {
            List<? extends List<RingEntry>> l1 = (List)s1;
            for (int i = 0; i < size; ++i) {
                List<RingEntry> list = l1.get(i);
                short key = key(list);
                indexes[i] = key;
            }
        } else {
            int i = 0;
            for (List<RingEntry> list : s1) {
                short key = key(list);
                indexes[i] = key;
                ++i;
            }

            if (i != indexes.length)
                throw new IllegalStateException("size does not match");
        }
        
        return AbstractRandomList.virtual(indexes.length, index -> EntryTables.this.get(indexes[index]));
    }

    public int count() {
        return tables.size();
    }

    public void stat(PrintStream out) {
        out.format("total: %d\n", metamap.size());
        metamap.forEach((key, value) -> out.format("%2d %3d\n", key + 2, value.size()));
    }
}
