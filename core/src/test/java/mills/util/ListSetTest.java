package mills.util;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.10.19
 * Time: 22:09
 */
public class ListSetTest {

    final List<Integer> base = List.of(1, 5, 7, 11);

    @Test
    public void testNaturalOrder() {


        List<Integer> mutable = new ArrayList<>(base);

        ListSet<Integer> list = ListSet.of();

        list.addAll(base);

        // based on a mutable list
        ListSet<Integer> list1 = ListSet.of(mutable);

        assertEquals(list, list1);

        list1.clear();

        list1.addAll(List.of(1, 11, 5, 7));
        assertEquals(list, list1);

        Set<Integer> iset =  new TreeSet<>(Arrays.asList(11, 7, 1, 5));
        assertEquals(list, iset);

        assertTrue(list.remove((Integer)7));
        assertTrue(iset.remove(7));
        assertEquals(list, iset);

    }

    @Test
    public void testBounds() {
        ListSet<Integer> entryTable = ListSet.of(base);
        TreeSet<Integer> intTable = new TreeSet<>(entryTable);

        assertEquals(entryTable, intTable);

        for(int i=0; i<12; ++i) {
            int index = entryTable.indexOf(i);
            int lower = entryTable.lowerBound(i);
            int upper = entryTable.upperBound(i);

            String message = String.format("entry %2d: %2d %2d %2d", i, index, lower, upper);
            //System.out.println(message);

            if(index>=0 && index< entryTable.size())
                assertTrue(i==entryTable.get(index), message);

            for(int k=0; k<lower; ++k)
                assertTrue(entryTable.get(k) < i, message);

            for(int k=lower; k< entryTable.size(); ++k)
                assertTrue(entryTable.get(k) >= i, message);

            for(int k=0; k<upper; ++k)
                assertTrue(entryTable.get(k) <= i, message);
            for(int k=upper; k< entryTable.size(); ++k)
                assertTrue(entryTable.get(k) > i, message);

            assertEquals(entryTable.headSet(i), intTable.headSet(i), "equal head");
            assertEquals(entryTable.tailSet(i), intTable.tailSet(i), "equal tail");
        }
    }

    @Test
    public void testParallelism() {
        ListSet<Integer> entryTable = ListSet.ofIndexed(64, i->i, i->i);
        entryTable.parallelStream().forEach(System.out::println);
    }
}