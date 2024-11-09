package mills.index.builder;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.util.listset.DirectPopMap;
import mills.util.listset.PopMap;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.01.21
 * Time: 21:31
 */
class Partitions extends DirectPopMap<Partition> {

    final PopMap<EntryTable> lePops;
    final PopMap<EntryTable> minPops;

    final ForkJoinPool pool;

    protected Partitions(ForkJoinPool pool, List<Partition> partitions, PopMap<EntryTable> lePops, PopMap<EntryTable> minPops) {
        super(PopCount.SRPOP, partitions);
        this.lePops = lePops;
        this.minPops = minPops;
        this.pool = pool;
    }

    @Override
    public Partition get(PopCount pop) {
        if(pop==null)
            return null;

        assert keySet.get(pop.index)==pop;

        return getValue(pop.index);
    }

    @Override
    public Partition getOf(int index) {

        assert keySet.get(index).index==index;

        return getValue(index);
    }

    public static Partitions create(ForkJoinPool pool) {

        var task = pool.submit(Partitions::partitions);

        PopMap<EntryTable> lePops = PopMap.lePops(Entries.TABLE);
        PopMap<EntryTable> minPops = PopMap.lePops(Entries.MINIMIZED);

        return new Partitions(pool, task.join(), lePops, minPops);
    }

    private static List<Partition> partitions() {

        Partition[] fragments = new Partition[PopCount.SRPOPS];
        Arrays.fill(fragments, Partition.EMPTY);

        PopCount.SRPOP.stream().filter(pop->pop.sum()<=8)
                .parallel()
                .forEach(pop -> fragments[pop.index] = Partition.of(pop));

        return List.of(fragments);
    }

    public static void main(String ... args) {

        Partitions pts = Partitions.create(ForkJoinPool.commonPool());

        pts.dump("root:", pt-> pt.root.isEmpty() ? "" :String.format("%5d", pt.root.size()));
        pts.dump("max frag size:", Partitions::maxFrag);
        pts.dump("tables:", pt->String.format("%5d", pt.tables.count()));
    }

    private static String maxFrag(Partition pt) {
        int max = pt.fragments.stream().flatMap(f->f.fragments.stream()).mapToInt(List::size).reduce(0, Math::max);
        return max==0 ? "" : String.format("%5d", max);
    }
}
