package mills.util;

import mills.ring.EntryTable;
import org.junit.Test;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  23.07.2015 09:54
 * modified by: $Author$
 * modified on: $Date$
 */
public class EntryTableTest {

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
