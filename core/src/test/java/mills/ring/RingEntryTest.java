package mills.ring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class RingEntryTest {

    @Test
    public void testCompareTo() throws Exception {
        RingEntry r7 = Entries.entry(7);
        RingEntry r77 = Entries.entry(77);

        int result = r7.compareTo(r77);

        assertTrue(result < 0);
    }
}