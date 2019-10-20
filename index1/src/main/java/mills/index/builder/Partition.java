package mills.index.builder;

import mills.bits.Perms;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomArray;

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

    private static final int[] PERMS = new int[]{0x01, 0x05, 0x0f, 0x11, 0x21, 0x41, 0x55, 0x81, 0xa5, 0xff};
    private static final int[] INDEX = index();

    public static final Partition EMPTY = new Partition() {
        @Override
        public String toString() {
            return "()";
        }
    };

    final EntryTable root;

    final List<Fragments> fragments;

    private Partition(EntryTable root, List<Fragments> fragments) {
        this.root = root;
        this.fragments = fragments;
    }

    private Partition() {
        root = EntryTable.EMPTY;
        fragments = AbstractRandomArray.constant(PERMS.length, Fragments.EMPTY);
    }

    public EntryTable root() {
        return root;
    }

    public Fragments get(int msk) {
        int index = INDEX[msk/2];

        if(index<0)
            return Fragments.EMPTY;

        return fragments.get(index);
    }

    private static int[] index() {
        int[] index = new int[Perms.VALUES.size()/2];
        Arrays.fill(index, -1);

        for(int i=0; i<PERMS.length; ++i) {
            int pi = PERMS[i];
            index[pi/2] = i;
        }

        return index;
    }

    public static Partition partition(PopCount pop, EntryTable root, EntryTables registry) {

        if(pop.max()>8)
            return null;

        root = root.filter(pop.eq);

        if(root.isEmpty())
            return null;

        root = registry.table(root);

        FragmentBuilder builder = new FragmentBuilder(registry);

        Map<EntryTable, Fragments> roots = new HashMap<>();
        Fragments[] fragments = new Fragments[PERMS.length];

        for (int i=0; i<PERMS.length; ++i) {
            int msk = PERMS[i];
            EntryTable filtered = i==0 ? root : root.filter(e -> (e.mlt&msk)==0);
            filtered = registry.table(filtered);
            Fragments fragment = roots.computeIfAbsent(filtered, builder::build);
            fragments[i] = fragment;
        }

        return new Partition(root, List.of(fragments)) {
            @Override
            public String toString() {
                return pop.toString();
            }
        };
    }

    public static Map<PopCount, Partition> partitions(EntryTable root, EntryTables registry) {

        Partition[] partitions = new Partition[PopCount.TABLE.size()];

        PopCount.TABLE.parallelStream()
                .forEach(pop -> partitions[pop.index] = partition(pop, root, registry));

        return PopTable.mapOf(Arrays.asList(partitions), EMPTY);
    }
}
