package mills.index2;

import mills.bits.PopCount;
import mills.index1.R0Table;
import mills.index1.R2Entry;
import mills.index1.R2Index;
import mills.index1.partitions.LePopTable;
import mills.index1.partitions.PartitionTables;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ForkJoinTask;
import java.util.function.UnaryOperator;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.09.2019 11:09
 * modified by: $
 * modified on: $
 */
public class IndexBuilder {

    final LePopTable lePopTable;
    final LePopTable minPopTable;

    final PartitionTables<EntryTable> partitions;

    private IndexBuilder(LePopTable lePopTable, LePopTable minPopTable,
                         PartitionTables<EntryTable> partitions) {
        this.lePopTable = lePopTable;
        this.minPopTable = minPopTable;
        this.partitions = partitions;
    }

    public static IndexBuilder create() {
        // build parallel
        ForkJoinTask<PartitionTables<EntryTable>> pt = ForkJoinTask.adapt(IndexBuilder::buildPartitions).fork();
        LePopTable lePopTable = LePopTable.build(Entries.TABLE);
        LePopTable minPopTable = LePopTable.build(Entries.MINIMIZED);
        PartitionTables<EntryTable> partitions = pt.join();
        return new IndexBuilder(lePopTable, minPopTable, partitions);
    }

    List<R2Index> asList() {
        return new AbstractRandomList<>() {
            @Override
            public int size() {
                return PopCount.TABLE.size();
            }

            @Override
            public R2Index get(int index) {
                return build(PopCount.TABLE.get(index));
            }
        };
    }

    private static PartitionTables<EntryTable> buildPartitions() {
        return PartitionTables.build(Entries.TABLE, UnaryOperator.identity());
    }

    public R2Index build(PopCount pop) {

        Map<RingEntry, R0Table> r0map = new ConcurrentSkipListMap<>();

        minPopTable.get(pop).forEach(e2->{
            R0Table t0 = t0(e2, pop);
            if(t0!=null && !t0.isEmpty())
                r0map.put(e2, t0);
        });

        List<R2Entry> t2 = new ArrayList<>(r0map.size());
        int index = 0;
        for (Map.Entry<RingEntry, R0Table> entry : r0map.entrySet()) {
            RingEntry i2 = entry.getKey();
            R0Table r0t = entry.getValue();
            index += r0t.range();
            R2Entry r2e = new R2Entry(index, i2.index, r0t);
            t2.add(r2e);
        }

        return new R2Index(pop, t2);
    }

    private R0Table t0(RingEntry e2, PopCount pop) {
        PopCount p2 = pop.sub(e2.pop);
        List<RingEntry> t0 = new ArrayList<>();
        List<EntryTable> tt1 = new ArrayList<>();

        EntryTable lt0 = lePopTable.get(p2);
        for (RingEntry e0 : lt0) {

            // while e0 < e2
            if(e0.compareTo(e2)>0)
                break;

            // skip entries that can be reduced further
            int mlt = e2.meq & e0.mlt;
            if(mlt!=0)
                continue;

            // remaining PopCount of e1[]
            PopCount p1 = p2.sub(e0.pop);

            EntryTable t1 = partitions.get(p1).get(0);
            if(t1.isEmpty())
                continue;

            // mask of stable permutations
            int msk = e2.meq & ~e0.mlt & 0xff;

            // those must not be reduced
            t1 = t1.filter(e1->(e1.mlt&msk)==0);
            if(t1.isEmpty())
                continue;

            t0.add(e0);
            tt1.add(t1);
        }

        return R0Table.of(t0, tt1);
    }
}
