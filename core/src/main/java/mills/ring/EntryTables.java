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
public class EntryTables extends AbstractRandomList<EntryTable> {

    static class TableEntry extends EntryArray {

        final Short key;

        protected TableEntry(short[] indices, Short key) {
            super(indices);
            this.key = key;
        }

        protected TableEntry(EntryArray entries, Short key) {
            super(entries.indices);
            this.key = key;
        }
    }

    static TableEntry entry(List<RingEntry> table, short key) {

        if(table instanceof TableEntry) {
            TableEntry entry = (TableEntry) table;
            if(entry.key == key)
                return entry;
        }

        if(table instanceof EntryArray) {
            return new TableEntry((EntryArray) table, key);
        }

        final int size = table.size();

        // should not happen...
        throw new IllegalStateException(String.format("unexpected TableEntry of size %s", size));

/*
        short index[] = new short[size];

        boolean ordered = true;
        RingEntry e = table.get(0);
        for(int i=1; i<size; i++) {
            RingEntry f = table.get(i);
            index[i] = f.index;
            ordered &= e.index>f.index;
        }

        if(!ordered)
            Arrays.sort(index);

        return new TableEntry(index, key);
*/
    }

    static final int OFFSET = RingEntry.MAX_INDEX+1;

    final List<TableEntry> tables = new ArrayList<>();

    final Map<List<RingEntry>, Short> entries = new ConcurrentSkipListMap<>(EntryTables::compare);

    public Short key(List<RingEntry> list) {

        if(list.isEmpty())
            return -1;

        // turn into singleton table.
        if(list.size()==1)
            return list.get(0).index;

        if(Entry.TABLE.equals(list))
            return RingEntry.MAX_INDEX;

        // try to up cast to TableEntry
        if(list instanceof TableEntry) {
            TableEntry entry = (TableEntry) list;
            Short key = entry.key;

            // verify key (might have been generated by other instance of EntryTables )
            int index = key - OFFSET;
            if(index>=0 && index<tables.size() && tables.get(index)==entry)
                return key;
        }

        Short key = entries.get(list);

        if(key!=null)
            return key;

        // generate in advance, might be recalculated
        //TableEntry table = entry(list, (key()));
        EntryTable table = EntryTable.of(list);

        return entries.computeIfAbsent(table, this::newEntry);
    }

    // generate a key
    private Short key() {
        int size = tables.size() + OFFSET;

        if(size>=Short.MAX_VALUE)
            throw new RuntimeException("too many entries");

        return (short) size;
    }

    private synchronized Short newEntry(List<RingEntry> table) {
        final TableEntry entry = entry(table, key());
        tables.add(entry);

        return entry.key;
    }

    public EntryTable table(List<RingEntry> list) {
        int index = key(list);

        EntryTable table = get(index);

        assert table!=null;

        return table;
    }

    public int size() {
        return OFFSET + tables.size();
    }

    public int count() {
        return tables.size();
    }

    @Override
    public EntryTable get(int index) {

        if(index==-1)
            return EntryTable.EMPTY;

        if(index< RingEntry.MAX_INDEX)
            return Entry.of(index).singleton;

        if(index== RingEntry.MAX_INDEX)
            return Entry.TABLE;

        return tables.get(index-OFFSET);
    }

    public List<EntryTable> build(List<Short> s1) {
        int size = s1.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1) {
            EntryTable table = EntryTables.this.get(s1.get(0));
            return Collections.singletonList(table);
        }

        short indexes[] = new short[size];

        for(int i=0; i<size; ++i)
            indexes[i] = s1.get(i);

        return new AbstractRandomList<EntryTable>() {

            @Override
            public int size() {
                return indexes.length;
            }

            @Override
            public EntryTable get(int index) {
                return EntryTables.this.get(indexes[index]);
            }
        };
    }

    public List<EntryTable> build(Collection<? extends List<RingEntry>> s1) {
        int size = s1.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1) {
            EntryTable table = EntryTables.this.table(s1.iterator().next());
            return Collections.singletonList(table);
        }

        short indexes[] = new short[size];
        int i=0;
        for (List<RingEntry> list : s1) {
            short key = key(list);
            indexes[i] = key;
            ++i;
        }

        if(i!=indexes.length)
            throw new IllegalStateException("size does not match");

        return AbstractRandomList.virtual(indexes.length, index -> EntryTables.this.get(indexes[index]));
    }


    public List<EntryTable> register(List<? extends List<RingEntry>> list) {

        int size = list.size();

        if(size==0)
            return Collections.emptyList();

        if(size==1) {
            List<RingEntry> table = list.get(0);
            if(table.size()>1)
                table = table(table);
            return Collections.singletonList(EntryTable.of(table));
        }

        short indexes[] = new short[size];

        for(int i=0; i<size; ++i) {
            final List<RingEntry> table = list.get(i);
            indexes[i] = key(table);
        }

        return new AbstractRandomList<EntryTable>() {

            @Override
            public int size() {
                return indexes.length;
            }

            @Override
            public EntryTable get(int index) {
                return EntryTables.this.get(indexes[index]);
            }
        };
    }

    public static int compare(List<RingEntry> t1, List<RingEntry> t2) {
        if(t1==t2)
            return 0;

        if(t1==null)
            return -1;

        if(t2==null)
            return 1;

        int result = Integer.compare(t1.size(), t2.size());
        if(result==0) {
            int l = t1.size();
            for(int i=0; i<l && result==0; ++i) {
                result = t1.get(i).compareTo(t2.get(i));
            }
        }

        return result;
    }

    public void stat(PrintStream out) {

        List<List<RingEntry>> tables = new ArrayList<>(entries.keySet());
        Collections.sort(tables, EntryTable.BY_SIZE);

        int size=0;
        int count=0;

        for (List<RingEntry> e : tables) {
            if(size==e.size())
                ++count;
            else {
                if(count>0)
                    out.format("%2d %3d\n", size, count);
                count = 1;
                size = e.size();
            }
        }

        if(count>0)
            out.format("%2d %3d\n", size, count);
    }
}
