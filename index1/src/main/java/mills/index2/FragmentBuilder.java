package mills.index2;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;
import mills.util.ArraySet;

import java.util.*;

import static mills.index2.Fragments.CLOPS;
import static mills.index2.Fragments.RADS;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:15
 */
public class FragmentBuilder {

    final EntryTables registry;

    final Set<EntryTable> roots = new TreeSet<>(Entries.BY_SIZE);

    static class Rads extends AbstractRandomList<EntryTable> {

        final List<RingEntry> list = new ArrayList<>();

        final EntryTable[] tables = new EntryTable[RADS];

        @Override
        public int size() {
            return RADS;
        }

        @Override
        public EntryTable get(int index) {
            EntryTable table = tables[index];
            return table==null ? EntryTable.EMPTY : table;
        }

        public EntryTable register(RingEntry rad, EntryTables registry) {
            EntryTable table = registry.table(list);
            list.clear();
            tables[rad.index] = table;
            return table;
        }

        public void clear() {
            list.clear();
            Arrays.fill(tables, null);
        }
    }

    final List<Rads> pool = new ArrayList<>();

    final Rads[] radlist = new Rads[CLOPS];

    FragmentBuilder(EntryTables registry) {
        this.registry = registry;
    }

    public Fragments build(EntryTable root) {
        try {
            return _build(root);
        } finally {
            clear();
        }
    }

    private void clear() {
        roots.clear();

        for (Rads rad : radlist) {
            if(rad!=null) {
                rad.clear();
                pool.add(rad);
            }
        }

        Arrays.fill(radlist, null);
    }

    private Rads rads(PopCount clop) {
        Rads rads = radlist[clop.index];

        if(rads==null) {
            if(pool.isEmpty())
                rads = new Rads();
            else
                rads = pool.remove(pool.size()-1);

            radlist[clop.index] = rads;
        }

        return rads;
    }

    static RingEntry rad(int index) {
        return Entries.RADIALS.get(index);
    }

    private static Map<RingEntry, EntryTable> asMap(List<EntryTable> tables) {
        return ArraySet.of(FragmentBuilder::rad, tables, EntryTable.EMPTY).asMap();
    }

    private static final Map<RingEntry, EntryTable> EMPTY = asMap(AbstractRandomList.constant(RADS, EntryTable.EMPTY));

    private Map<RingEntry, EntryTable> fragment(int index) {
        Rads rads = radlist[index];
        if(rads==null)
            return null;

        List<EntryTable> tables = registry.register(rads);
        return asMap(tables);
    }


    Fragments _build(EntryTable root) {

        Entries.RADIALS.forEach(rad -> process(rad, root));

        List<Map<RingEntry, EntryTable>> tables = AbstractRandomArray.generate(CLOPS, this::fragment);
        Map<PopCount, Map<RingEntry, EntryTable>> fragments = ArraySet.of(PopCount::get, tables, null).asMap();

        return new Fragments(fragments, roots);
    }

    void process(RingEntry rad, EntryTable root) {

        for (Rads rads : radlist) {
            if(rads!=null)
                rads.list.clear();
        }

        for (RingEntry e : root) {
            PopCount clop = e.clop().add(e.and(rad).pop);
            if(clop.max()<4) {
                rads(clop).list.add(e);
            }
        }

        for (Rads rads : radlist) {
            if(rads!=null) {
                EntryTable table = rads.register(rad, registry);
                roots.add(table);
            }
        }
    }
}
