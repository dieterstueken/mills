package mills.partitions;

import mills.bits.PGroup;
import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/15/15
 * Time: 3:57 PM
 */
class Generator {

    final Map<Set<PGroup>, Partition> partitions = new HashMap<>();

    final EntryTables registry = new EntryTables();

    public final List<Partition> table = AbstractRandomList.generate(128, this::partition);

    static Set<PGroup> gset(int mlt) {
        Set<PGroup> gset = EnumSet.allOf(PGroup.class);
        gset.removeIf(pg -> pg.collides(mlt));
        return gset;
    }

    Partition partition(Set<PGroup> gset) {
        EntryTable root = RingEntry.MINIMIZED.filter(e -> gset.contains(e.grp()));
        root = registry.table(root);

        List<EntryTable> partitions = new ArrayList<>();

        for (PopCount pop : PopCount.TABLE.subList(0, PopCount.P88.index)) {
            EntryTable part = root.filter(e->e.pop==pop);
            partitions.add(part);
        }

        partitions = registry.register(partitions);

        return new Partition(root, partitions, registry);
    }

    private Partition partition(int mlt) {
        Partition p = partitions.computeIfAbsent(Generator.gset(mlt), this::partition);
        return p;
    }

    public static List<Partition> partitions() {
        return new Generator().table;
    }

    public static void main(String ... args) {

        Generator g = new Generator();

        for (int i = 0, tableSize = g.partitions.size(); i < tableSize; i++) {
            Partition partition = g.partitions.get(i);

            EntryTable t33 = partition.pop(PopCount.of(3,3).index);
            int k = t33.filter(e -> e.clop()==PopCount.EMPTY).size();

            System.out.format("partition %02x [%d] %2d %2d\n", i, partition.root.size(), t33.size(), k);


            for(int nb=0; nb<9; ++nb) {
                for(int nw=0; nw<9; ++nw) {
                    PopCount pop = PopCount.of(nb,nw);
                    System.out.format("%3d", partition.pop(pop.index).size());
                }
                System.out.println();
            }
            System.out.println();


        }

    }
}
