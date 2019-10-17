package mills.index2;

import mills.bits.Perms;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomArray;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.10.19
 * Time: 17:23
 */
public class Partition {

    public static final List<Perms> PERMS = Perms.listOf(0x00, 0x04, 0x0e, 0x10, 0x20, 0x40, 0x54, 0x80, 0xa4, 0xfe);

    public static final Partition EMPTY = new Partition();

    final List<Fragments> fragments;

    private Partition(List<Fragments> fragments) {
        this.fragments = fragments;
    }

    private Partition() {
        fragments = AbstractRandomArray.constant(PERMS.size(), Fragments.EMPTY);
    }

    public EntryTable root() {
        return fragments.get(0).root();
    }

    public Fragments get(int msk) {
        int index = INDEX[msk];

        if(index<0)
            return null;

        return fragments.get(index);
    }

    private static final int[] INDEX = new int[PERMS.size()];

    static{
        Arrays.fill(INDEX, -1);
        for(int i=0; i<PERMS.size(); ++i) {
            INDEX[i] = i;
        }
    }

    public static Partition build(EntryTable root, EntryTables registry) {

        if(root.isEmpty())
            return EMPTY;

        FragmentBuilder builder = new FragmentBuilder(registry);

        Map<EntryTable, Fragments> roots = new HashMap<>();
        List<Fragments> fragments = new ArrayList<>(PERMS.size());

        for (Perms perm : PERMS) {
            int msk = perm.getIndex();
            EntryTable filtered = msk==0 ? root : root.filter(e -> (e.mlt&msk)==0);
            filtered = registry.table(filtered);
            Fragments frag = roots.computeIfAbsent(filtered, builder::build);
            fragments.add(frag);
        }

        return new Partition(List.copyOf(fragments));
    }
}
