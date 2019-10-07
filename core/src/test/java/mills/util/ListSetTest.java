package mills.util;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.19
 * Time: 22:09
 */
public class ListSetTest {

    @Test
    public void testNaturalOrder() {

        ListSet<Integer> list = ListSet.of();

        list.add(1);
        list.add(5);
        list.add(11);
        list.add(7);

        ListSet<Integer> list1 = ListSet.of();
        list1.addAll(Arrays.asList(11, 7, 1, 5));

        assertEquals(list, list1);

        list1.clear();

        list1.addAll(Arrays.asList(1, 5, 11, 7));
        assertEquals(list, list1);

        List<Integer> list2 =  Arrays.asList(1, 5, 7, 11);
        assertEquals(list, list2);
    }

    @Test
    public void testBounds() {
        ListSet<Integer> entryTable = ListSet.of(1, 5, 7, 11);
        TreeSet<Integer> intTable = new TreeSet<>(entryTable);

        assertEquals(entryTable, intTable);

        for(int i=0; i<12; ++i) {
            int index = entryTable.indexOf(i);
            int lower = entryTable.lowerBound(i);
            int upper = entryTable.upperBound(i);

            String message = String.format("entry %2d: %2d %2d %2d", i, index, lower, upper);
            //System.out.println(message);

            if(index>=0 && index< entryTable.size())
                assertTrue(message, i==entryTable.get(index));

            for(int k=0; k<lower; ++k)
                assertTrue(message, entryTable.get(k) < i);

            for(int k=lower; k< entryTable.size(); ++k)
                assertTrue(message, entryTable.get(k) >= i);

            for(int k=0; k<upper; ++k)
                assertTrue(message, entryTable.get(k) <= i);
            for(int k=upper; k< entryTable.size(); ++k)
                assertTrue(message, entryTable.get(k) > i);

            assertEquals("equal head", entryTable.headSet(i), intTable.headSet(i));
            assertEquals("equal tail", entryTable.tailSet(i), intTable.tailSet(i));
        }
    }
}