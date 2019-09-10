package mills.ring;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class RingEntryTest {

    @Test
    public void testCompareTo() throws Exception {
        RingEntry r7 = Entry.of(7);
        RingEntry r77 = Entry.of(77);

        int result = r7.compareTo(r77);

        assertTrue(result < 0);
    }
}