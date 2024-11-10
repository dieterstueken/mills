package mills.bits;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 08.11.24
 * Time: 18:06
 */
class PopCountTest {

    @Test
    public void testPops() {

        assertEquals(PopCount.P44.index+1, PopCount.NCLOPS);

        PopCount.TABLE.forEach(this::testPop);
    }

    void testPop(PopCount pop) {
        int index = PopCount.getIndex(pop.nb, pop.nw);
        assertEquals(pop.index, index);

        if(pop.nb > pop.nw)
            assertEquals(0, pop.index%2);

        if(pop.nb < pop.nw)
            assertEquals(1, pop.index%2);

        if(pop.max()<=4)
            assertTrue(index < 5*5);

        if(pop.sum()<9)
            assertTrue(index < 9 * 10 / 2);

        int start = pop.index<25 ? 0 : 25;

        for(int i=start; i<pop.index; ++i) {
            PopCount lt = PopCount.TABLE.get(i);

            assertTrue(lt.sum() <= pop.sum(), ()->String.format("sum: %s[%d]=%d >= %s[%d]=%d",
                    lt, lt.index, lt.sum(),
                    pop, pop.index, pop.sum()));

            if(lt.sum() == pop.sum()) {
                assertTrue(lt.div() <= pop.div(), ()->String.format("div: %s[%d]=%d >= %s[%d]=%d",
                        lt, lt.index, lt.div(),
                        pop, pop.index, pop.div()));
            }
        }
    }
}