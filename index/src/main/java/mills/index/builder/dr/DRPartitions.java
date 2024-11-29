package mills.index.builder.dr;

import mills.bits.DRPop;
import mills.index.fragments.Partition;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.Table;
import mills.util.listset.ListMap;

import java.util.List;

public class DRPartitions extends ListMap<DRPop, Partition> {

    private DRPartitions(List<Partition> partitions) {
        super(DRPop.TABLE, partitions);
    }

    public static DRPartitions create() {

        EntryTable root = Entries.MINIMIZED;

        List<Partition> partitions = DRPop.TABLE.stream()
                .map(p44->root.filter(p44::eq))
                .map(Partition::empty)
                .toList();

        return new DRPartitions(partitions);
    }

    public static void main(String[] args) {
        DRPartitions pts = create();

        System.out.println("root");
        Table.of(DRPop::of).map(pts::get)
                .map(pt->pt.root.size())
                .map("%5d"::formatted, "     ")
                .dump(15, "%5d");

        System.out.println("pops");
        Table.of(DRPop::of).map(DRPop::pop)
                .map(pop -> 16*pop.nb + pop.nw)
                .map(" %02x"::formatted, "     ")
                .dump(15, "%3d");
    }
}
