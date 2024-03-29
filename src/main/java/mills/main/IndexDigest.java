package mills.main;

import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Position;
import mills.ring.Entries;
import mills.util.IntegerDigest;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.ForkJoinTask;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  08.10.12 11:18
 * modified by: $Author$
 * modified on: $Date$
 */
public class IndexDigest {

    final IntegerDigest digest = new IntegerDigest("MD5");

    protected final IndexProvider indexes = IndexProvider.load();


    //final List<? extends PosIndex> table = IndexBuilder.table();
    //final List<? extends PosIndex> table = Indexes.build(executor).table();

    IndexDigest() throws NoSuchAlgorithmException {}

    ForkJoinTask<? extends PosIndex> start(int nb, int nw) {
        PopCount pop = PopCount.of(nb, nw);
        return ForkJoinTask.adapt(() -> indexes.build(pop)).fork();
    }

    int analyze(ForkJoinTask<? extends PosIndex> task) {
        if(task!=null) {
            PosIndex posIndex = task.join();
            List<Position> posList = posIndex.positions();
            PopCount pop = posIndex.pop();
            int range = posIndex.range();
            int n20 = posIndex.n20();
            System.out.format("l%d%d%10d, %4d\n", pop.nb, pop.nw, range, n20);
            digest.update(range);
            return range;
        }
        return 0;
    }

    public void run() {
        System.out.format("start %d\n", Entries.TABLE.size());
        double start = System.currentTimeMillis();

        ForkJoinTask<? extends PosIndex> task = null;
        long total = 0;
        //for(PopCount pop:PopCount.TABLE) {
        for(int nb=0; nb<10; ++nb)
        for(int nw=0; nw<10; ++nw) {
            ForkJoinTask<? extends PosIndex> next = start(nb, nw);
            total += analyze(task);
            task = next;
        }

        total += analyze(task);

        double stop = System.currentTimeMillis();
        System.out.format("%.3f s\n", (stop - start) / 1000);

        System.out.format("total: %s\n" , total);

        System.out.println("digest: " + digest);
    }

    static void verify(PosIndex posIndex) {
        posIndex.process((index, i201) -> {
            int i = posIndex.posIndex(i201);
            if(i!=index)
                throw new IllegalStateException();
            long j201 = posIndex.i201(index);
            if(j201!=i201)
                throw new IllegalStateException(); });
    }

    // e1f9dd6500301e4649063163f3c0d633

    public static void main(String ... args) throws NoSuchAlgorithmException, IOException {

        new IndexDigest().run();

        //System.in.read();
    }
}
