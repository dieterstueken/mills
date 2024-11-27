package mills.index.drindex;

import mills.bits.DRPop;
import mills.bits.PopCount;
import mills.index.fragments.Partition;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.listset.ListMap;

import java.util.List;
import java.util.function.Function;

public class DRPartitions extends ListMap<DRPop, Partition> {

    private DRPartitions(List<Partition> partitions) {
        super(DRPop.TABLE, partitions);
    }

    public static DRPartitions create() {

        EntryTable root = Entries.MINIMIZED;

        List<Partition> partitions = DRPop.TABLE.stream()
                .map(p44->root.filter(p44::eq))
                .map(Partition::of)
                .toList();

        return new DRPartitions(partitions);
    }

    public void dump(String head, Function<Partition, Integer> dump) {

        System.out.println(head);

        System.out.print("r/d ");
        for (int nr = 0; nr <= 14; ++nr) {
            PopCount pop = PopCount.get(nr);
            System.out.format("   %02d", pop.nb + 10*pop.nw);
        }

        System.out.println();

        for (int nr = 0; nr <= 14; ++nr) {
            PopCount pr = PopCount.get(nr);
            System.out.format(" %02d:", pr.nb + 10*pr.nw);
            for (int nd = 0; nd <= 14; ++nd) {
                PopCount pd = PopCount.get(nd);
                DRPop p44 = DRPop.of(pd, pr);
                Partition pt = get(p44);
                System.out.format("%5d", dump.apply(pt));
            }
            System.out.println();
        }

        System.out.println();
    }

    public static void main(String[] args) {
        DRPartitions pts = create();

        pts.dump("root", pt->pt.root.size());
    }
}
