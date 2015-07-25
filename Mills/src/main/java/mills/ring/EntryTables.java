package mills.ring;

import mills.util.AbstractRandomList;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12/21/14
 * Time: 8:49 PM
 */
public class EntryTables extends AbstractRandomList<EntryTable> {

    final List<EntryTable> tables = new ArrayList<>();

    final Map<List<RingEntry>, Short> entries = new ConcurrentHashMap<>();

    public Short index(List<RingEntry> table) {

        if(table.isEmpty())
            return -1;

        // turn into singleton table.
        if(table.size()==1)
            return table.get(0).index;

        Short key = entries.get(table);

        if(key!=null)
            return key;

        synchronized (this) {
            // beware:
            // value may have been added just before by an other thread.
            // thus an additional check is needed before finally adding.
            return entries.computeIfAbsent(EntryTable.of(table), REGISTER);
        }
    }

    private final Function<List<RingEntry>, Short> REGISTER = table -> {
        int size = tables.size() + RingEntry.MAX_INDEX;
        tables.add(EntryTable.of(table));

        if(size>=Short.MAX_VALUE)
            throw new RuntimeException("too many entries");

        return (short) size;
    };

    public EntryTable table(List<RingEntry> table) {
        int index = index(table);
        return get(index);
    }

    public int size() {
        return RingEntry.MAX_INDEX + tables.size();
    }

    public int count() {
        return tables.size();
    }

    @Override
    public EntryTable get(int index) {

        if(index==-1)
            return EntryTable.EMPTY;

        if(index<RingEntry.MAX_INDEX)
            return RingEntry.of(index).singleton;
        else {
            index -= RingEntry.MAX_INDEX;
            return tables.get(index);
        }
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
            indexes[i] = index(table);
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

    public void stat(PrintStream out) {
        int size=0;
        int count=0;

        for (List<RingEntry> e : entries.keySet()) {
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
