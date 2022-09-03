package mills.index.builder;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.PopMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.21
 * Time: 21:31
 */
public class Partitions extends PopMap<Partition> {

    final PopMap<EntryTable> lePops;
    final PopMap<EntryTable> minPops;

    protected Partitions(List<Partition> partitions, PopMap<EntryTable> lePops, PopMap<EntryTable> minPops) {
        super(PopCount.TABLE, partitions);
        this.lePops = lePops;
        this.minPops = minPops;
    }

    @Override
    public Partition get(PopCount pop) {

        assert keySet.get(pop.index)==pop;

        return getValue(pop.index);
    }

    @Override
    public Partition getOf(int index) {

        assert keySet.get(index).index==index;

        return getValue(index);
    }

    public static Partitions create() {
        var task = ForkJoinTask.adapt(Partitions::partitions).fork();

        PopMap<EntryTable> lePops = PopMap.lePops(Entries.TABLE);
        PopMap<EntryTable> minPops = PopMap.lePops(Entries.MINIMIZED);

        return new Partitions(task.join(), lePops, minPops);
    }

    private static List<Partition> partitions() {

        Partition[] fragments = new Partition[PopCount.SIZE];
        Arrays.fill(fragments, Partition.EMPTY);

        PopCount.TABLE.stream().filter(pop->pop.sum()<=8)
                .parallel()
                .forEach(pop -> fragments[pop.index] = Partition.of(pop));

        return List.of(fragments);
    }

    public static void main(String ... args) {

        Partitions pts = Partitions.create();

        pts.dump("root:", pt->String.format("%5d", pt.root.size()));
        pts.dump("max frag size:", pt-> {
            int max = 0;
            for (Fragments fm : pt.fragments) {
                max = Math.max(max, fm.root.size());
            }
            return String.format("%5d", pt.root.size());
        }
        );
        pts.dump("tables:", pt->String.format("%5d", pt.tables.count()));
    }
}
