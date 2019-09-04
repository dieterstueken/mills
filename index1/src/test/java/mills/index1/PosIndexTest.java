package mills.index1;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PosIndexTest {

    final IndexList indexes = IndexList.create();

    @Test
    public void testFindIndex() {

        PosIndex index = indexes.get(PopCount.of(4,5));

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

        PosIndex index = indexes.get(PopCount.of(0,0));

        int range = index.range();

        //assertEquals(range, 1);

    }

}