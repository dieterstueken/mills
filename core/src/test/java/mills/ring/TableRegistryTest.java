package mills.ring;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.11.24
 * Time: 18:02
 */
class TableRegistryTest {

    static List<RingEntry> entries(int ... keys) {
        List<RingEntry> entries = new ArrayList<>();
        for (int key : keys) {
            entries.add(Entries.entry(key));
        }

        return entries;
    }

    TableRegistry r1 = new TableRegistry();
    TableRegistry r2 = new TableRegistry();

    @Test
    public void testRegistry() {

        List<RingEntry> entries = entries(3,5,7);

        for(int j=0; j<entries.size(); ++j) {
            testEntries(entries.subList(0, j));
        }
    }

    void testEntries(List<RingEntry> entries) {
        IndexedEntryTable t1 = r1.getTable(entries);
        testTables(entries, t1);
        assertSame(t1, r1.getTable(t1));

        IndexedEntryTable t2 = r2.getTable(entries);
        testTables(entries, t1);

        testTables(t1, t2);

        IndexedEntryTable t3 = r2.getTable(t1);
        testTables(entries, t3);
        testTables(t2, t3);
    }

    static void testTables(List<RingEntry> e1, List<RingEntry> e2) {
        assertEquals(e1, e2);
        assertEquals(e2, e1);

        if(e1.hashCode() !=e2.hashCode())
            assertEquals(e1.hashCode(), e2.hashCode());

        int size = e1.size();
        if(size>0) {
            testTables(e1.subList(0, size-1), e2.subList(0, size-1));
        }
    }

    @Test
    public void testList() {
        List<RingEntry> l1 = List.of(Entries.entry(3));
        List<RingEntry> l0 = Entries.entry(3).singleton();
        testTables(l0, l1);
    }
}