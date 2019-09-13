package mills.index1.main;

import mills.bits.PGroup;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.bits.Sector;
import mills.index1.Partitions;
import mills.index1.partitions.PartitionTable;
import mills.ring.Entry;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/20/14
 * Time: 5:47 PM
 */
public class PartitionStat {

    final Partitions partitions = Partitions.build();

    final List<Predicate<RingEntry>> filters;

    PartitionStat() {

        filters = new ArrayList<>(81*25);

        final List<Function<RingEntry, PopCount>> radials = AbstractRandomList.generate(81, PartitionStat::radial);

        for (PopCount clop : PopCount.TABLE.subList(0, 25)) {
            for (Function<RingEntry, PopCount> r : radials) {
                Predicate<RingEntry> filter = e -> e.clop().add(r.apply(e)).equals(clop);
                filters.add(filter);
            }
        }
    }

    public void run() {

        int total = 0;
        int difft = 0;

        for (PopCount pop : PopCount.TABLE) {

            PartitionTable<EntryTable> pt = partitions.partitions.get(pop);
            EntryTable root = pt.get(0);
            if(root.isEmpty())
                continue;

            Set<PGroup> pset = PGroup.groups(root);

            Set<EntryTable> tset = new TreeSet<>(EntryTable.BY_SIZE);
            List<EntryTable> tables = pt.tables;

            for (EntryTable t : tables) {
                for (Predicate<RingEntry> f : filters) {
                    EntryTable ft = t.filter(f);
                    int size = ft.size();
                    if(size>1 && size!=t.size()) {
                        tset.add(ft);
                        ++total;
                    }
                }
            }

            System.out.format("%s %2d %2d %2d %03x +%3d\n",
                    pop, root.size(), pt.tables.size(), pset.size(), PGroup.code(pset), tset.size());

            difft += tset.size();
        }

        System.out.format("%d different of %d total tables\n", difft, total);
    }

    public static void main(String ... args) {
        new PartitionStat().run();
    }

    public static Function<RingEntry, PopCount> radial(int i) {

        return new Function<RingEntry, PopCount>() {

            final RingEntry radials = Entry.RADIALS.get(i);

            PopCount clop(RingEntry ringEntry, Sector sector) {
                Player p1 = ringEntry.player(sector);
                return p1==Player.None || p1==radials.player(sector) ? p1.pop : PopCount.EMPTY;
            }

            @Override
            public PopCount apply(RingEntry e) {
                PopCount clop = PopCount.EMPTY;

                clop = clop.add(clop(e, Sector.N));
                clop = clop.add(clop(e, Sector.E));
                clop = clop.add(clop(e, Sector.S));
                clop = clop.add(clop(e, Sector.W));

                return clop;
            }
        };
    }
}
