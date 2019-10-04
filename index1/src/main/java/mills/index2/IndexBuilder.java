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

    final List<EntryTable> partitions;

    final List<EntryTable[]> fragments = AbstractRandomList.generate(PopCount.SIZE, pop -> new EntryTable[128]);

    private IndexBuilder(LePopTable lePopTable, LePopTable minPopTable,
                         List<EntryTable> partitions) {
        this.lePopTable = lePopTable;
        this.minPopTable = minPopTable;
        this.partitions = partitions;
    }

    public static IndexBuilder create() {
        LePopTable lePopTable = LePopTable.build(Entries.TABLE);
        LePopTable minPopTable = LePopTable.build(Entries.MINIMIZED);
        AbstractRandomList<EntryTable> partitions = AbstractRandomList.transform(PopCount.TABLE, pop->pop.sum()<=8 ? Entries.TABLE.filter(pop.eq) : EntryTable.EMPTY);
        return new IndexBuilder(lePopTable, minPopTable, partitions.copyOf());
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

        lePopTable.get(pop).parallelStream().forEach(e2->{
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

        EntryTable lt0 = minPopTable.get(p2);
        for (RingEntry e0 : lt0) {

            if(e0.index>e2.index)
                break;

            // skip entries that can be reduced further
            int mlt = e2.mlt20(e0);
            if(mlt!=0)
                continue;

            // remaining PopCount of e1[]
            PopCount p1 = p2.sub(e0.pop);

            EntryTable t1 = partitions.get(p1.index);
            if(t1.isEmpty())
                continue;

            // mask of stable permutations
            int msk = meq20(e2, e0);

            EntryTable tf = fragment(p1, t1,  msk);

            if(tf.isEmpty())
                continue;

            t0.add(e0);
            tt1.add(tf);
        }

        return R0Table.of(t0, tt1);
    }

    EntryTable fragment(PopCount p1, EntryTable t1, int msk) {
        EntryTable[] ft = fragments.get(p1.index);

        EntryTable tf = ft[msk/2];

        if(tf==null) {
            tf = fragment(t1, msk);
            ft[msk/2] = tf;
        }

        return tf;
    }

    static EntryTable fragment(EntryTable t1, int msk) {
        return t1.filter((e1->(e1.mlt&msk)==0));
    }

    static int meq20(RingEntry e2, RingEntry e0) {
        // ether both are stable
        int meq = e2.meq & e0.meq & 0xff;

        // e2 reduced and e0 increases
        int msk = e2.mlt & ~e0.meq & ~e0.mlt & 0xff;

        for(int i=1; i<8; ++i) {
            int m = 1<<i;
            if(m>msk)
                break;
            if((msk&m)!=0) {
                if(e2.perm(i)==e0.index && e0.perm(i)==e2.index)
                    meq |= m;
            }
        }


        return meq;
    }
}
