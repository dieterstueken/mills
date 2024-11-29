package mills.index.fragments;

import mills.ring.*;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * A Partition has a root set of entries of equal pop count.
 */
public class Partition {

    public final TableRegistry tables;

    public final IndexedEntryTable root;

    public final List<Fragments> fragments;

    private Partition(TableRegistry tables, EntryTable root, List<Fragments> fragments) {
        this.tables = tables;
        this.root = tables.getTable(root);
        this.fragments = fragments;
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public Fragments getFragments(int meq) {
        return fragments.get(meq/2);
    }

    public String toString() {
        return String.format("p[%d]", root.size());
    }

    /**
     * An empty Partition with all empty fragments.
     */
    private static class Empty extends Partition {

        Empty() {
            super(new TableRegistry(),
                    EntryTable.empty(),
                    AbstractRandomList.constant(128, Fragments.EMPTY));
        }

        // singe instance needed ever.
        static final Empty INSTANCE = new Empty();

        // prepare a common ZERO instance.
        final Partition zero = newSingleton(this.tables, Entries.empty());

        /**
         * Derive a new singleton partition sharing this table registry.
         * @param entry forming the table.
         * @return a new singleton Partition or even ZERO.
         */
        Partition singleton(RingEntry entry) {
            return entry.isEmpty() ? zero : newSingleton(this.tables, entry);
        }
    }

    /**
     * Create a Partition with a single entry.
     * All fragments become the same.
     * This is static by design to decouple the instance from Empty.
     * @param entry for a single value table.
     * @return a new singleton Partition.
     */
    private static Partition newSingleton(TableRegistry tables, RingEntry entry) {
        IndexedEntryTable root = entry.singleton();
        List<Fragments> fragments = AbstractRandomList.constant(128, Fragments.of(root));
        return new Partition(tables, root, fragments);
    }

    public static Partition empty() {
        return Empty.INSTANCE;
    }

    public static Partition zero() {
        return Empty.INSTANCE.zero;
    }

    public static Partition empty(RingEntry entry) {
        return Empty.INSTANCE.singleton(entry);
    }

    public static Partition empty(EntryTable root) {
        if(root.isEmpty())
            return empty();

        if(root.size()==1)
            return empty(root.first());

        TableRegistry tables = new TableRegistry();
        IndexedEntryTable indexed = tables.getTable(root);
        List<Fragments> fragments = Fragments.list(indexed, tables);
        return new Partition(tables, indexed, fragments);
    }
}
