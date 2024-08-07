package mills.ring;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.*;

public class EntryTableTest {

    final EntryTable entryTable = EntryTable.of(2, 3, 7, 9);
    final SortedSet<Integer> intTable = new TreeSet<>(Arrays.asList(2, 3, 7, 9));

    @Test
    public void testFindIndex() {

        for(int i=0; i<11; ++i) {
            RingEntry e = Entries.entry(i);
            int index = entryTable.indexOf(e);
            int lower = entryTable.lowerBound(e.index());
            int upper = entryTable.upperBound(e.index());

            String message = String.format("entry %2d: %2d %2d %2d", e.index, index, lower, upper);
            //System.out.println(message);
            if(index>=0 && index< entryTable.size())
                assertEquals(e, entryTable.get(index), message);

            for(int k=0; k<lower; ++k)
                assertTrue(entryTable.get(k).index < i, message);
            for(int k=lower; k< entryTable.size(); ++k)
                assertTrue(entryTable.get(k).index >= i, message);

            for(int k=0; k<upper; ++k)
                assertTrue(entryTable.get(k).index <= i, message);
            for(int k=upper; k< entryTable.size(); ++k)
                assertTrue(entryTable.get(k).index > i);


            testEqual("equal head", entryTable.headSet(e), intTable.headSet(i));
            testEqual("equal tail", entryTable.tailSet(e), intTable.tailSet(i));
        }
    }

    @Test
    public void testSubSet() {

        StringBuilder sb = new StringBuilder();

        for(int i=0; i<11; ++i) {
            RingEntry e1 = Entries.entry(i);

            for(int k=i; k<11; ++k) {

                sb.setLength(0);
                sb.append("subset: ").append(i).append(" - ").append(k);

                RingEntry e2 = Entries.entry(k);
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

    @Test
    public void testCreation() {
        assertThrows(IllegalArgumentException.class, () -> EntryTable.of(2, 3, 10, 9));
    }

    @Test
    public void testIndexOf() {

        testIndexOf(Entries.TABLE);
        testIndexOf(Entries.MINIMIZED);
        testIndexOf(Entries.entry(77).singleton());
        testIndexOf(EntryTable.empty());

    }

    private void testIndexOf(EntryTable rt) {
        for (RingEntry e : rt) {
            int index = rt.indexOf(e);
            assertEquals(e, rt.get(index));
        }
    }

    private void testEqual(String message, EntryTable et, Set<Integer> is) {

        assertEquals(et.size(), is.size(), message);

        for (RingEntry e : et) {
            assertTrue(is.contains((int) e.index), message);
        }
    }

    @Test
    public void testSubListFind() {

        EntryTable sample = EntryTable.of(100, 110, 120, 130);
        EntryTable subSample = sample.subList(1, 3);

        for(int i=90; i<150; i+=5) {
            int i1 = sample.findIndex(i);
            int i2 = subSample.findIndex(i);

            System.out.format("%3d: %3d %3d\n", i, i1, i2);
        }
    }
}