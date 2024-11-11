package mills.index.fragments;

import mills.bits.Perms;
import mills.ring.*;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.21
 * Time: 22:40
 */
public class Fragments {

    public final IndexedEntryTable root;

    public final List<IndexedEntryTables> fragments;

    Fragments(IndexedEntryTable root, List<IndexedEntryTables> fragments) {
        this.root = root;
        this.fragments = fragments;
    }

    public List<IndexedEntryTable> get(RingEntry rad) {
        return fragments.get(rad.radix());
    }

    public String toString() {
        return String.format("f[%d]", root.size());
    }

    ////////////////////////////////////////////////

    static final Fragments EMPTY = new Fragments(IndexedEntryTable.of(), AbstractRandomArray.constant(81, IndexedEntryTables.of()));

    public static final List<Perms> ROOTS;
    public static final List<Predicate<RingEntry>> FILTERS;
    public static final List<Integer> ROOT_INDEX;

    static  {
        ROOTS = Perms.listOf(0x01, 0x05, 0x0f, 0x11, 0x21, 0x41, 0x55, 0x81, 0xa5, 0xff);
        FILTERS = AbstractRandomList.map(ROOTS, perm -> e->e.stable(perm));

        List<Integer> lookup = AbstractRandomList.preset(128, -1);
        for (int i = 0; i < ROOTS.size(); i++) {
            Perms p = ROOTS.get(i);
            lookup.set(p.getIndex()/2, i);
        }
        ROOT_INDEX = List.copyOf(lookup);
    }

    static Fragments of(IndexedEntryTable root) {
        List<IndexedEntryTables> fragments = AbstractRandomArray.constant(81, IndexedEntryTables.of(root));
        return new Fragments(root, fragments);
    }

    static Fragments of(IndexedEntryTable root, TableRegistry registry) {
        if(root.isEmpty())
            return EMPTY;

        if(root.size()==1) {
            return of(root);
        }

        Map<IndexedEntryTables, IndexedEntryTables> fragset = new HashMap<>();

        // build a IndexedEntryTable list for each radial.
        // different tables are unified by fragset.

        List<IndexedEntryTables> fragments = Entries.RADIALS.stream()
                // split root into groups of same clop for each rad
                .map(rad ->root.stream().collect(Collectors.groupingBy(rad::clops)))
                // each group is converted into a IndexedEntryTables
                .map(group -> registry.tablesOf(group.values()))
                // canonicalize different IndexedEntryTables (hashCode?)
                .map(tables -> fragset.computeIfAbsent(tables, UnaryOperator.identity()))
                .toList();

        return new Fragments(root, fragments) {
            public String toString() {
                return String.format("f[%d:%d]", root.size(), fragset.size());
            }
        };
    }

    public static List<Fragments> list(EntryTable root, TableRegistry tables) {

        Fragments[] fragments = new Fragments[ROOTS.size()];

        for(int i=0; i<9; ++i) {
            Predicate<RingEntry> filter = FILTERS.get(i);
            IndexedEntryTable table = tables.getTable(root.filter(filter));
            fragments[i] = Fragments.of(table, tables);
        }

        // this is the only case of a duplicate fragment:
        // for size==8  with 1:0, 7:0, 7:1 and swapped
        // fragments[2] == fragments[9]
        IndexedEntryTable table = tables.getTable(root.filter(FILTERS.get(9)));
        if(root.size()==8) {
            Fragments f2 = fragments[2];
            if(f2.root!=table)
                throw new IllegalStateException("should be equal");
            fragments[9] = f2;
        } else
            fragments[9] = Fragments.of(table, tables);

        // spread root fragments to Fragments[128]
        return new AbstractRandomArray<>(128) {

            @Override
            public Fragments get(int meq) {
                int index = ROOT_INDEX.get(meq/2);
                return fragments[index];
            }
        };
    }

}
