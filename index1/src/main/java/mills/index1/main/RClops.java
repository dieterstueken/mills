package mills.index1.main;

import mills.bits.PopCount;
import mills.bits.RClop;
import mills.index1.partitions.PartitionTables;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.Stat;

import java.util.*;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  17.09.2019 10:10
 * modified by: $
 * modified on: $
 */
public class RClops {

    private static final RClops EMPTY = new RClops();

    public static RClops of(EntryTable root) {
        return root.isEmpty() ? EMPTY : new RClops(root);
    }

    final EntryTable root;
    final Set<RClop> rclops;
    final Map<EntryTable, Set<RClop>> tables = new TreeMap<>(Entries.BY_SIZE);

    private RClops(EntryTable root) {
        this.root = root;
        this.rclops = clopset(root);

        RClop.TABLE.stream()
                .map(rc->root.filter(rc::matches))
                .forEach(this::add);
    }

    private RClops() {
        root = EntryTable.EMPTY;
        rclops = Collections.emptySet();
    }

    private void add(EntryTable et) {
        if(!et.isEmpty())
            tables.computeIfAbsent(et, RClops::clopset);
    }

    static Set<RClop> clopset(EntryTable et) {
        Set<RClop> rcs = new TreeSet<>();
        et.forEach(e -> rcs.add(RClop.of(e)));
        return rcs;
    }

    @Override
    public String toString() {
        return String.format("RC[%d:%d:%d]", root.size(), rclops.size(), tables.size());
    }

    public static void main(String ... args) {

        PartitionTables<RClops> pt = PartitionTables.build(Entries.TABLE, RClops::of);

        for (int nb = 0; nb < 10; nb++) {
            for (int nw = 0; nw < 10; nw++) {
                PopCount pop = PopCount.of(nb, nw);
                RClops rc = pt.get(pop).get(0);
                if(!rc.root.isEmpty())
                    System.out.format("%4d:%2d", rc.root.size(), rc.tables.size());
            }
            System.out.println();
        }

        RClops total = RClops.of(Entries.MINIMIZED);

        System.out.println();

        Set<EntryTable> tables = new TreeSet<>(Entries.BY_SIZE);

        pt.values().stream()
                .flatMap(t -> t.tables.stream())
                .flatMap(rc->rc.tables.keySet().stream())
                .forEach(tables::add);

        Stat stat = new Stat();
        
        stat.process(tables.stream());

        stat.dump("total");
    }
}
