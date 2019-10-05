package mills.index2;

import mills.bits.Perm;
import mills.bits.Perms;
import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.R0Table;
import mills.index1.R2Table;
import mills.index1.partitions.LePopTable;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

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

    List<PosIndex> asList() {
        return new AbstractRandomList<>() {
            @Override
            public int size() {
                return PopCount.TABLE.size();
            }

            @Override
            public PosIndex get(int index) {
                return build(PopCount.TABLE.get(index));
            }
        };
    }

    public R2Table build(PopCount pop) {
        ConcurrentSkipListMap<RingEntry, R0Table> r0map = new ConcurrentSkipListMap<>();

        minPopTable.get(pop).stream().forEach(e2->{
            R0Table t0 = t0(e2, pop);
            if(t0!=null && !t0.isEmpty())
                r0map.put(e2, t0);
        });

        EntryTable t2 = EntryTable.of(r0map.keySet());
        List<R0Table> r0t = List.copyOf(r0map.values());

        return R2Table.of(pop, t2, r0t);
    }

    private R0Table t0(RingEntry e2, PopCount pop) {
        PopCount p2 = pop.sub(e2.pop);
        List<RingEntry> t0 = new ArrayList<>();
        List<EntryTable> tt1 = new ArrayList<>();

        // all r0>=r2
        EntryTable lt0 = lePopTable.get(p2).tailSet(e2);
        for (RingEntry e0 : lt0) {

            // e0 can be reduced further while e2 remains stable.
            if((e0.mlt&e2.min)!=0)
                continue;

            // remaining PopCount of e1[]
            PopCount p1 = p2.sub(e0.pop);

            EntryTable t1 = partitions.get(p1.index);
            if(t1.isEmpty())
                continue;

            // mask of stable permutations
            int meq = e2.meq & e0.meq & 0xff;
            if(e0!=e2 && e0.min()==e2.index) {
                // test e0 minimums if equals e2
                for(Perms pm = Perms.of(e0.pmin()); !pm.isEmpty(); pm = pm.next()) {
                    Perm p = pm.first();
                    if(e2.perm(p.ordinal())==e0.index)
                        meq |= p.msk();
                }
            }
            EntryTable tf = fragment(p1, t1,  meq);

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
