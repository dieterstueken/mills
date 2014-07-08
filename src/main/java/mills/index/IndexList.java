package mills.index;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mills.bits.PopCount;
import mills.util.AbstractRandomList;
import mills.util.FutureReference;

import java.util.List;
import java.util.function.Supplier;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.12.12 14:58
 * modified by: $Author$
 * modified on: $Date$
 */
public class IndexList extends AbstractRandomList<R2Index> {

    final List<? extends Supplier<R2Index>> tables;

    private IndexList(List<? extends Supplier<R2Index>> tables) {
        this.tables = tables;
    }

    public static IndexList create() {
        Partitions partitions = Partitions.open();
        final List<? extends FutureReference<R2Index>> list = Lists.transform(partitions, FutureReference::new);
        List<FutureReference<R2Index>> tables = ImmutableList.copyOf(list);

        return new IndexList(tables);
    }

    @Override
    public R2Index get(int index) {
        return tables.get(index).get();
    }

    public R2Index get(PopCount pop) {
        return get(pop.index);
    }

    @Override
    public int size() {
        return PopCount.TABLE.size();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String... args) {

        IndexList indexes = create();

        for (int i = 0; i < 1000; ++i) {
            int p = PopCount.SIZE;
            p *= Math.random();

            R2Index t = indexes.get(p);

            PopCount pop = PopCount.get(p);

            System.out.format("%d:%d %9d\n", pop.nb, pop.nw, t.size());
        }
    }
}
