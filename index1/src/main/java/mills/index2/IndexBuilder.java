package mills.index2;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.R0Table;
import mills.index1.R2Entry;
import mills.index1.R2Table;
import mills.index1.partitions.LePopTable;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

        List<R2Entry> table = minPopTable.get(pop).parallelStream()
                .map(e2 -> r2t0(e2, pop))
                .filter(Objects::nonNull)
                .sorted(R2Entry.R2)
                .collect(Collectors.toList());

        EntryTable t2 = EntryTable.of(AbstractRandomList.transform(table, R2Entry::r2));
        List<R0Table> r0t = AbstractRandomList.transform(table, R2Entry::t0).copyOf();

        return R2Table.of(pop, t2, r0t);
    }

    private R2Entry r2t0(RingEntry e2, PopCount pop) {
        R0Table t0 = t0(e2, pop);
        return t0.isEmpty()? null : new R2Entry(e2, t0);
    }

    private R0Table t0(RingEntry e2, PopCount pop) {
        PopCount p2 = pop.sub(e2.pop);
        List<RingEntry> t0 = new ArrayList<>();
        List<EntryTable> tt1 = new ArrayList<>();

        EntryTable lt0 = lePopTable.get(p2);
        for (RingEntry e0 : lt0) {

            // e2 is minimized.
            // if e0 may be minimized to a smaller value they may be swapped.
            if(e0.min()>e2.index)
                continue;
            
            // remaining PopCount of e1[]
            PopCount p1 = p2.sub(e0.pop);

            EntryTable t1 = partitions.get(p1.index);
            if(t1.isEmpty())
                continue;

            int meq = meq(e0, e2);
            if(meq==0)
                continue;

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

    /**
     * Return a perm mask of all stable permutations.
     * If any permutation reduces r20 return 0.
     * Else bit #0 is set.
     * @param e2 entry on ring 2.
     * @param e0 entry on ring 0 (minimized).
     * @return a perm mask of all stable permutations or 0.
     */
    static int meq(RingEntry e2, RingEntry e0) {

        // no further analysis necessary.
        if(e0==e2)
            return e0.meq & 0xff;

        // may be reduced easily
        int mlt = e0.meq & e2.mlt & 0xff;
        if (mlt != 0) // unstable anyway
            return 0;

        // ether both are stable
        int meq = e2.meq & e0.meq & 0xff;

        // no swap possible if e2 has an other minimum
        if (e0.index != e2.min())
            return meq;

        // analyze all minima
        int min = e2.min & 0xff;
        while (min != 0) {
            int mi = Integer.lowestOneBit(min);
            min ^= mi;
            int i = Integer.numberOfTrailingZeros(mi);

            // even reduces
            if (e0.perm(i) < e2.index) {
                return 0;
            }

            // also stable with swap
            if (e0.perm(i) == e2.index) {
                meq |= mi;
            }
        }

        return meq;
    }
}
