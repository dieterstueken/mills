package mills.index2;

import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomArray;
import mills.util.ArraySet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.10.19
 * Time: 17:23
 */
public class Partition {

    public static final List<Perms> PERMS = Perms.listOf(0x01, 0x05, 0x0f, 0x11, 0x21, 0x41, 0x55, 0x81, 0xa5, 0xff);

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
        int index = INDEX[msk/2];

        if(index<0)
            return Fragments.EMPTY;

        return fragments.get(index);
    }

    private static final int[] INDEX = new int[Perms.VALUES.size()/2];

    static {
        Arrays.fill(INDEX, -1);
        for(int i=0; i<PERMS.size(); ++i) {
            Perms p = PERMS.get(i);
            INDEX[p.getIndex()/2] = i;
        }
    }

    public static Partition partition(PopCount pop, EntryTable root, EntryTables registry) {

        if(pop.max()>8)
            return null;

        root = root.filter(pop.eq);

        if(root.isEmpty())
            return null;

        FragmentBuilder builder = new FragmentBuilder(registry);

        Map<EntryTable, Fragments> roots = new HashMap<>();
        Fragments[] fragments = new Fragments[PERMS.size()];

        for (Perms perm : PERMS) {
            int msk = perm.getIndex();
            EntryTable filtered = msk==0 ? root : root.filter(e -> (e.mlt&msk)==0);
            filtered = registry.table(filtered);
            Fragments fragment = roots.computeIfAbsent(filtered, builder::build);
            fragments[perm.getIndex()] = fragment;
        }

        return new Partition(List.of(fragments));
    }

    public static Map<PopCount, Partition> partitions(EntryTable root, EntryTables registry) {

        Partition[] partitions = new Partition[PopCount.TABLE.size()];

        PopCount.TABLE
                .stream()
                .forEach(pop -> partitions[pop.index] = partition(pop, root, registry));

        return ArraySet.of(PopCount::get, partitions, EMPTY).asMap();
    }
}
