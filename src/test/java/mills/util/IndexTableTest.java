package mills.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IndexTableTest {

    final List<Integer> list = Arrays.asList(2, 4, 1);
    final IndexTable index = IndexTable.sum0(list, i -> i);

    @Test
    public void testLowerBound() throws Exception {

        for(int i=0; i<list.size(); ++i) {
            System.out.format("%d: %d -> %d\n", i, list.get(i), index.get(i));
        }

        for(int i=-2; i<10; ++i) {
            int k = index.lowerBound(i);
            System.out.format("%d %d\n", i, k);
        }
    }

    @Test
    public void testRange() throws Exception {
        int range = index.range();
        //assertEquals(range, 28);
    }
}
