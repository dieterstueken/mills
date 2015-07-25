package mills.ring;

import org.junit.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class EntryTableTest {

    final EntryTable entryTable = EntryTable.of(2, 3, 7, 9);
    SortedSet<Integer> intTable = new TreeSet<>(Arrays.asList(2, 3, 7, 9));

    @Test
    public void testFindIndex() throws Exception {

        for(int i=0; i<11; ++i) {
            RingEntry e = RingEntry.of(i);
            int index = entryTable.indexOf(e);
            int lower = entryTable.lowerBound(e.index());
            int upper = entryTable.upperBound(e.index());

            String message = String.format("entry %2d: %2d %2d %2d", e.index, index, lower, upper);
            //System.out.println(message);

            if(index>=0 && index< entryTable.size())
                assertTrue(message, e.equals(entryTable.get(index)));

            for(int k=0; k<lower; ++k)
                assertTrue(message, entryTable.get(k).index < i);
            for(int k=lower; k< entryTable.size(); ++k)
                assertTrue(message, entryTable.get(k).index >= i);

            for(int k=0; k<upper; ++k)
                assertTrue(message, entryTable.get(k).index <= i);
            for(int k=upper; k< entryTable.size(); ++k)
                assertTrue(message, entryTable.get(k).index > i);

            testEqual("equal head", entryTable.headSet(e), intTable.headSet(i));
            testEqual("equal tail", entryTable.tailSet(e), intTable.tailSet(i));
        }
    }

    @Test
    public void testSubSet() throws Exception {

        StringBuilder sb = new StringBuilder();

        for(int i=0; i<11; ++i) {
            RingEntry e1 = RingEntry.of(i);

            for(int k=i; k<11; ++k) {

                sb.setLength(0);
                sb.append("subset: ").append(i).append(" - ").append(k);

                RingEntry e2 = RingEntry.of(k);
                EntryTable st = entryTable.subSet(e1, e2);

                String sep = " {";
                for(RingEntry e:st) {
                    sb.append(sep).append(e.index);
                    sep = ", ";
                }

                if(st.isEmpty())
                    sb.append(sep);

                sb.append("}");

                SortedSet<Integer> si = intTable.subSet(i, k);
                testEqual(sb.toString(), st, si);
            }
        }
    }

    @Test(expected= AssertionError.class)
    public void testCreation() throws Exception {
        EntryTable.of(2, 3, 10, 9);
    }

    @Test
    public void testIndexOf() {

        testIndexOf(RingEntry.TABLE);
        testIndexOf(RingEntry.MINIMIZED);
        testIndexOf(RingEntry.of(77).singleton);
        testIndexOf(EntryTable.EMPTY);

    }

    private void testIndexOf(EntryTable rt) {
        for (RingEntry e : rt) {
            int index = rt.indexOf(e);
            assertEquals(e, rt.get(index));
        }
    }

    private void testEqual(String message, EntryTable et, Set<Integer> is) {

        assertEquals(message, et.size(), is.size());

        for (RingEntry e : et) {
            assertTrue(message, is.contains((int) e.index));
        }
    }
}