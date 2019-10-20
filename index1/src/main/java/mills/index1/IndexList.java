package mills.index1;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.util.FutureReference;

import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  07.12.12 14:58
 * modified by: $Author$
 * modified on: $Date$
 */
public class IndexList implements IndexProvider {

    final List<FutureReference<R2Index>> tables;

    private IndexList(List<FutureReference<R2Index>> tables) {
        this.tables = tables;
    }

    public IndexList() {
        this(createTables());
    }

    private static List<FutureReference<R2Index>> createTables() {
        Partitions partitions = Partitions.build();
        return FutureReference.of(partitions);
    }

    public static IndexList create() {
        return new IndexList();
    }

    public R2Index build(PopCount pop) {
        return tables.get(pop.index).get();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public static void main(String... args) {

        IndexList indexes = create();

        for (int i = 0; i < 1000; ++i) {
            int p = PopCount.SIZE;
            p *= Math.random();

            PopCount pop = PopCount.get(p);

            R2Index t = indexes.build(pop);

            System.out.format("%d:%d %9d\n", pop.nb, pop.nw, t.range());
        }
    }
}
