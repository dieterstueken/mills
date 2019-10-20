package mills.index.builder;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.ArraySet;
import mills.util.ListSet;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:15
 */
public class FragmentBuilder {

    static final int CLOPS = PopCount.CLOSED.size();
    static final int RADS = Entries.RADIALS.size();

    final EntryTables registry;

    Set<EntryTable> roots = null;

    final List<ListSet<RingEntry>> tables = AbstractRandomList.generate(CLOPS, i -> ListSet.mutable());

    public FragmentBuilder(EntryTables registry) {
        this.registry = registry;
    }

    private void clear() {
        tables.forEach(List::clear);
        roots = null;
    }

    static RingEntry rad(int index) {
        return Entries.RADIALS.get(index);
    }

    private static Map<RingEntry, EntryTable> asMap(List<EntryTable> tables) {
        return ArraySet.of(FragmentBuilder::rad, tables, EntryTable.EMPTY).asMap();
    }

    private static final Map<RingEntry, EntryTable> EMPTY = asMap(AbstractRandomList.constant(RADS, EntryTable.EMPTY));

    Fragments build(EntryTable root) {

        if(roots!=null)
            throw new IllegalStateException("concurrent build");

        try {
            roots = new TreeSet<>(Entries.BY_SIZE);

            for (RingEntry e : root) {
                process(e.radials(), e);
            }

            Map<PopCount, Fragment> fragments = ArraySet.of(PopCount::get, fragments(), Fragment.EMPTY).asMap();

            return new Fragments(fragments, root, roots);
        } finally {
            clear();
        }
    }

    void process(RingEntry rad, RingEntry e) {
        PopCount clop = e.clop(rad);

        // ignore excessive closes
        if(clop.max()<=4) {
            tables.get(clop.index).add(e);
        }

        for (Sector sector : rad.sectors()) {
            RingEntry next = rad.withPlayer(sector, Player.None);
            process(next, e);
        }
    }

    List<Fragment> fragments() {
        return AbstractRandomList.generate(CLOPS, this::fragment);
    }

    Fragment fragment(int i) {

        PopCount clop = PopCount.get(i);
        EntryTable table = registry.table(tables.get(i));
        roots.add(table);

        return Fragment.of(clop, table, registry);
    }
}
