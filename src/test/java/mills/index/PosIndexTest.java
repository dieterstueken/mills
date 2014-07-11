package mills.index;

import mills.bits.PopCount;
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

        IndexProcessor verify = new IndexProcessor() {
            @Override
            public void process(int posIndex, long i201) {
                assertEquals("posIndex", posIndex, index.posIndex(i201));
                assertEquals("i201", i201, index.i201(posIndex));
            }
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