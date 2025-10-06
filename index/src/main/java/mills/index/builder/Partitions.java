package mills.index.builder;

import mills.bits.PopCount;
import mills.index.fragments.Partition;
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
        super(PopCount.POPS88, partitions);
        this.lePops = lePops;
        this.minPops = minPops;
        this.pool = pool;
    }

    @Override
    public Partition get(PopCount pop) {
        if(pop==null)
            return null;

        assert pop.index >= size() || keySet.get(pop.index) == pop;

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

        Partition[] partitions = new Partition[PopCount.NPOPS88];
        Arrays.fill(partitions, Partition.of());

        PopCount.POPS88.stream().filter(pop->pop.sum()<=8)
                .parallel()
                .forEach(pop -> partitions[pop.index] = partition(pop));

        return List.of(partitions);
    }

    private static Partition partition(PopCount pop) {

        // empty table
        if(pop.sum()>8) {
            return Partition.of();
        }

        // single entry: [RingEntry(0)]
        if(pop.sum()==0) {
            return Partition.zero();
        }

        return Partition.of(Entries.TABLE.filter(pop.eq));
    }


    public static void main(String ... args) {

        Partitions pts = Partitions.create(ForkJoinPool.commonPool());

        pts.dumpInt("root:", pt-> pt.isEmpty() ? null : pt.root.size());
        pts.dumpInt("max frag size:", Partitions::maxFrag);
        pts.dumpInt("tables:", pt-> pt.tables.count());
    }

    private static Integer maxFrag(Partition pt) {
        int max = pt.fragments.stream()
                .flatMap(f->f.fragments.stream())
                .mapToInt(List::size)
                .reduce(0, Math::max);
        return max==0 ? null : max;
    }
}
