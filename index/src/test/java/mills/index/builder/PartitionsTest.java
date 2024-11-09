package mills.index.builder;

import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.Stat;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import static mills.ring.RingEntry.MAX_INDEX;

class PartitionsTest {

    @Test
    void testPartitions() {
        Partitions partitions = IndexTests.timer("partitons", () -> Partitions.create(ForkJoinPool.commonPool()));

        partitions.dump("root:", pt->String.format("%5d", pt.root.size()));
        partitions.dump("tables:", pt->String.format("%5d", pt.tables.count()));

        Stat stat = new Stat();

        partitions.values().stream().map(p->p.tables)
                .map(t->t.subList(MAX_INDEX + 1, t.size()))
                .flatMap(Collection::stream)
                //.filter(PartitionsTest::isCompact)
                .mapToInt(Collection::size)
                .forEach(stat);
              
        stat.dump("stat");

    }

    static boolean isCompact(EntryTable table) {
        RingEntry e = table.getFirst();
        for(int i=1; i<table.size(); i++) {
            RingEntry f = table.get(i);
            if(f.index != e.index+1)
                return false;
            e = f;
        }

        return true;
    }
    
}