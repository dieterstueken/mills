package mills.index1;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.ring.Entries;
import mills.util.IntegerDigest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PosIndexTest {

    final IndexList indexes = IndexList.create();

    @Test
    public void testFindIndex() {

        PosIndex index = indexes.get(PopCount.get(4,5));

        System.out.format("%s: %d\n", index.pop().toString(), index.range());

        int m = index.range()/2;
        long i201 = index.i201(m);
        int posIndex = index.posIndex(i201);

        assertEquals(posIndex, m);

        IndexProcessor verify = (posIndex1, j201) -> {
            assertEquals("posIndex", posIndex1, index.posIndex(j201));
            assertEquals("i201", j201, index.i201(posIndex1));
            //System.out.format("%d %d\n", posIndex1, j201);
        };

        index.process(verify);
    }

    @Test
    public void testSize() {

        PosIndex index = indexes.get(PopCount.get(0,0));

        int range = index.range();

        //assertEquals(range, 1);

    }

    @Test
    public void testDigest() {
        final IntegerDigest digest = new IntegerDigest("MD5");
        System.out.format("start %d\n", Entries.TABLE.size());

        double start = System.currentTimeMillis();

        for (int nb = 0; nb < 10; ++nb)
            for (int nw = 0; nw < 10; ++nw) {
                PopCount pop = PopCount.of(nb, nw);
                R2Index posIndex = indexes.get(pop);
                int range = posIndex.range();
                int n20 = posIndex.n20();
                System.out.format("l%d%d%10d, %4d\n", pop.nb, pop.nw, range, n20);
                digest.update(range);

                verify(posIndex);
            }

        double stop = System.currentTimeMillis();
        System.out.format("%.3f s\n", (stop - start) / 1000);

        System.out.println("digest: " + digest.digest());
    }
    
    static void verify(R2Index posIndex) {
        posIndex.process((index, i201) -> {
            int i = posIndex.posIndex(i201);
            if(i!=index)
                throw new IllegalStateException();
            long j201 = posIndex.i201(index);
            if(j201!=i201)
                 throw new IllegalStateException();
        });
    }
}