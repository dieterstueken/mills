package mills.index1;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
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
public class IndexList extends AbstractRandomList<PosIndex> implements IndexProvider {

    final List<? extends Supplier<R2Index>> tables;

    private IndexList(List<? extends Supplier<R2Index>> tables) {
        this.tables = tables;
    }

    public IndexList() {
        this(createTables());
    }

    private static List<? extends Supplier<R2Index>> createTables() {
        Partitions partitions = Partitions.open();
        final List<? extends FutureReference<R2Index>> list = AbstractRandomList.transform(partitions, FutureReference::new);
        return List.copyOf(list);
    }

    public static IndexList create() {
        return new IndexList();
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

            System.out.format("%d:%d %9d\n", pop.nb, pop.nw, t.range());
        }
    }
}
