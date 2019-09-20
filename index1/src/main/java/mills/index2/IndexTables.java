package mills.index2;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.R2Index;
import mills.ring.Entries;
import mills.util.IntegerDigest;
import mills.util.PopMap;

import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.09.2019 10:17
 * modified by: $
 * modified on: $
 */
public class IndexTables extends PopMap<R2Index> {

    public IndexTables(List<R2Index> indexes) {
        super(indexes);
    }

    public static IndexTables build() {
        return new IndexTables(IndexBuilder.create().asList());
    }

    public static void main(String... args) throws NoSuchAlgorithmException {
        IntegerDigest digest = new IntegerDigest("MD5");

        IndexBuilder indexes = IndexBuilder.create();
        System.out.format("start %d\n", Entries.TABLE.size());
        double start = System.currentTimeMillis();

        //for(PopCount pop:PopCount.TABLE) {
        for(int nb=0; nb<10; ++nb)
            for(int nw=0; nw<10; ++nw) {
                PopCount pop = PopCount.of(nb, nw);
                PosIndex posIndex = indexes.build(pop);

                int range = posIndex.range();
                int n20 = posIndex.n20();
                System.out.format("l%d%d%10d, %4d\n", pop.nb, pop.nw, range, n20);
                digest.update(range);
            }

        double stop = System.currentTimeMillis();
        System.out.format("%.3f s\n", (stop - start) / 1000);

        System.out.println("digest: " + digest.digest());
    }
}
