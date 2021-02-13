package mills.index.builder2;

import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.IndexedEntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.02.21
 * Time: 21:20
 */
public class Partition {

    final Tables tables = new Tables();

    final IndexedEntryTable root;

    final List<Fragments> fragments;

    public Partition(EntryTable root) {
        this.root = tables.get(root);
        this.fragments = fragments();
    }

    public boolean isEmpty() {
        return root.isEmpty();
    }

    public String toString() {
        return String.format("p[%d]", root.size());
    }

    public Fragments getFragments(int meq2) {
        int index = ROOT_INDEX.get(meq2/2);
        return index<0 ? null : fragments.get(index);
    }

    IndexedEntryTable filter(Predicate<? super RingEntry> predicate) {
        return tables.get(root.filter(predicate));
    }

    IndexedEntryTable filter(Perms perms) {
        return filter(e -> e.stable(perms));
    }

    private List<Fragments> fragments() {
        if(root.size()<2)
            return AbstractRandomList.constant(ROOTS.size(), Fragments.of(root, tables));

        Fragments[] fragments = new Fragments[ROOTS.size()];

        for(int i=0; i<9; ++i) {
            IndexedEntryTable table = filter(ROOTS.get(i));
            fragments[i] = fragments(table);
        }

        // this is the only case of a duplicate fragment:
        // for size==8  with 1:0, 7:0, 7:1 and swapped
        // fragments[2] == fragments[9]
        IndexedEntryTable table = filter(ROOTS.get(9));
        if(root.size()==8) {
            Fragments f2 = fragments[2];
            if(f2.root!=table)
                throw new IllegalStateException("should be equal");
            fragments[9] = f2;
        } else
            fragments[9] = fragments(table);

        return List.of(fragments);
    }

    private Fragments fragments(IndexedEntryTable table) {
        return Fragments.of(table, tables);
    }

    public static final List<Perms> ROOTS;
    public static final List<Integer> ROOT_INDEX;

    static  {
        ROOTS = Perms.listOf(0x01, 0x05, 0x0f, 0x11, 0x21, 0x41, 0x55, 0x81, 0xa5, 0xff);
        List<Integer> lookup = AbstractRandomList.preset(128, -1);
        for (int i = 0; i < ROOTS.size(); i++) {
            Perms p = ROOTS.get(i);
            lookup.set(p.getIndex()/2, i);
        }
        ROOT_INDEX = List.copyOf(lookup);
    }

    public static Partition EMPTY = new Partition(EntryTable.EMPTY);
            // AbstractRandomList.constant(ROOTS.size(), AbstractRandomList.constant(Entries.RADIALS.size(), EntryTable.EMPTY)));

    public static Partition of(PopCount pop) {
        
        if(pop.sum()>8)
            return EMPTY;

        EntryTable root = PopCount.EMPTY.equals(pop) ? Entries.EMPTY.singleton: Entries.TABLE.filter(pop.eq);

        return new Partition(root);
    }
}
