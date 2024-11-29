package mills.index.builder;

import mills.bits.PopCount;
import mills.index.fragments.Partition;
import mills.index.tables.C2Table;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.listset.ListSet;
import mills.util.listset.PopMap;

import java.util.List;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 11:36
 */
abstract public class GroupBuilder {

    protected final PopCount pop;

    final EntryTable t2;

    final ListSet<PopCount> clops;

    final PopMap<C2Builder> builders = PopMap.allocate(PopCount.NCLOPS);

    final C2Table[] tables = new C2Table[PopCount.NCLOPS];

    public GroupBuilder(PopCount pop, EntryTable t2) {
        this.pop = pop;
        this.t2 = t2;

        // subset of clops to build
        PopCount mclop = pop.mclop(false);

        clops = ListSet.of(PopCount.CLOPS.stream()
                .filter(mclop::ge).toList());

        clops.forEach(this::setupBuilder);
    }

    private void setupBuilder(PopCount clop) {
        builders.put(clop, new C2Builder(clop, t2));
    }


    public PopMap<C2Table> build(Function<C2Builder, C2Table> generator) {

        clops.parallelStream()
                .map(builders::get)
                .map(generator)
                .forEach(this::put);

        List<C2Table> results = clops.transform(this::get).copyOf();

        return PopMap.of(clops, results);
    }

    abstract protected EntryTable t0(RingEntry r2);

    abstract protected Partition partition(RingEntry r2, RingEntry r0);

    private void put(C2Table result) {
        tables[result.clop().index] = result;
    }

    private C2Table get(PopCount pop) {
        return tables[pop.index];
    }

    RingEntry limit(RingEntry r2, RingEntry r0) {
        return null;
    }
}
