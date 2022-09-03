package mills.partitions;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.IntSummaryStatistics;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  27.07.2015 10:22
 * modified by: $Author$
 * modified on: $Date$
 */
public class PGroupTest {

    @Test
    public void testLindex() throws Exception {

        IntStream.range(0, 512).forEach(igrp ->{

            EnumSet<PGroup> groups = groups(igrp);

            IntStream.range(0, 128).forEach(msk ->{
                EnumSet<PGroup> subset = subset(groups, msk);

                IntSummaryStatistics stat = IntStream.range(0, 128)
                        .filter(i -> subset(subset, i).equals(subset))
                        .summaryStatistics();

                int min = PGroup.lindex(subset, msk);
                int max = PGroup.pindex(subset, msk);

                //System.out.format("%03x %02x %02x %02x %3d\n", igrp, msk, min, max, stat.getCount());

                assertEquals(stat.getMax(), max);
                assertEquals(stat.getMin(), min);
            });
        });
    }

    static EnumSet<PGroup> groups(int igrp) {
        EnumSet<PGroup> groups = EnumSet.noneOf(PGroup.class);

        for (PGroup pg : PGroup.values()) {
            if((igrp&(1<<pg.ordinal()))!=0)
                groups.add(pg);
        }

        return groups;
    }

    static EnumSet<PGroup> subset(EnumSet<PGroup> groups, int msk) {
        EnumSet<PGroup> subset = EnumSet.copyOf(groups);
        subset.removeIf(pg->pg.collides(msk));
        return subset;
    }
}