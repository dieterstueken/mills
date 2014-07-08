package mills.main;

import mills.bits.PopCount;
import mills.index.IndexList;
import mills.index.R2Index;
import mills.ring.RingEntry;
import mills.util.IntegerDigest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.RecursiveAction;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  08.10.12 11:18
 * modified by: $Author$
 * modified on: $Date$
 */
public class IndexDigest extends RecursiveAction {

    final IntegerDigest digest = new IntegerDigest("MD5");

    protected final IndexList indexes = IndexList.create();


    //final List<? extends PosIndex> table = IndexBuilder.table();
    //final List<? extends PosIndex> table = Indexes.build(executor).table();

    IndexDigest() throws NoSuchAlgorithmException {}

    public void compute() {
        System.out.format("start %d\n", RingEntry.TABLE.size());
        double start = System.currentTimeMillis();

        for(PopCount pop:PopCount.TABLE) {
            final R2Index posIndex = indexes.get(pop);
            final int range = posIndex.size();
            int n20 = posIndex.entries().size();

            System.out.format("l%d%d%10d, %4d\n", pop.nb, pop.nw, range, n20);
            digest.update(range);
        }

        double stop = System.currentTimeMillis();
        System.out.format("%.3f s\n", (stop - start) / 1000);

        System.out.println("digest: " + digest.digest());

        //executor.shutdown();
    }

    public static void main(String ... args) throws NoSuchAlgorithmException, IOException {
        new IndexDigest().invoke();
    }
}
