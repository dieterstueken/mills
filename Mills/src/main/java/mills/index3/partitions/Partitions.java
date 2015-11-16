package mills.index3.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 20:12
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partitions extends PartitionTable<MaskTable> {

    final List<MaskTable> partitions;

    final List<EntryTable> lePop;

    public Partitions(List<MaskTable> partitions, List<EntryTable> lePop) {
        this.partitions = partitions;
        this.lePop = lePop;
    }

    public static Partitions build() {
        return new Builder(new EntryTables()).invoke();
    }

    public static void main(String... args) {

        final EntryTables registry = new EntryTables();

        Partitions partitions = new Builder(registry).invoke();

        System.out.format("build %d\n", registry.count());


        Set<EntryTable> es = new TreeSet<>(EntryTable.BY_SIZE);

        for (MaskTable mt : partitions.content()) {
            for (RadialTable rt : mt.content()) {
                for (ClopTable ct : rt.content()) {
                    es.addAll(ct.content());
                }
            }
        }

        int n = 0, size = 0;

        for (EntryTable e : es) {
            if (e.size() != size) {
                //if(n>0)
                System.out.format("%2d %4d\n", size, n);
                n = 1;
                size = e.size();
            } else
                ++n;
        }

        if (n > 0)
            System.out.format("%2d %4d\n", size, n);

        System.out.format("total %4d\n", es.size());
    }

    @Override
    public MaskTable get(int index) {
        return partitions.get(index);
    }

    public EntryTable lePop(PopCount pop) {
        return lePop.get(pop.index);
    }

    @Override
    public int size() {
        return 100;
    }

    @Override
    public List<MaskTable> content() {
        return partitions;
    }

    public MaskTable get(PopCount pop) {
        return get(pop.index);
    }
}
