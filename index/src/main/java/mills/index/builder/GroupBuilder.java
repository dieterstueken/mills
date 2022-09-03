package mills.index.builder;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.tables.R0Table;
import mills.ring.EntryMap;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;
import mills.util.ListSet;
import mills.util.PopMap;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 11:36
 */
class GroupBuilder {

    final Partitions partitions;

    final PopCount pop;

    final EntryTable t2;

    final ListSet<PopCount> clops;

    final PopMap<C2Builder> builders = PopMap.allocate(PopCount.NCLOPS);

    GroupBuilder(Partitions partitions, PopCount pop) {
        this.partitions = partitions;
        this.pop = pop;
        this.t2 = partitions.minPops.get(pop);

        // subset of clops to build
        PopCount mclop = pop.mclop(false);

        clops = ListSet.of(PopCount.CLOPS.stream()
                .filter(mclop::ge).toList());

        clops.forEach(this::setupBuilder);
    }

    private void setupBuilder(PopCount clop) {
        builders.put(clop, new C2Builder(clop, t2));
    }

    <R extends Clops> PopMap<R> build(BiFunction<PopCount, EntryMap<R0Table>, R> generator) {

        buildEntries();

        List<R> tmp = AbstractRandomList.preset(PopCount.NCLOPS, null);

        clops.parallelStream()
                .map(builders::get)
                .map(b -> b.build(generator))
                .forEach(result -> tmp.set(result.clop().index, result));

        List<R> results = clops.transform(c -> tmp.get(c.index)).copyOf();

        return PopMap.of(clops, results);
    }

    private void buildEntries() {
        T0Builder builder = new T0Builder(this);
        builder.invoke();
    }
}
