package mills.index2;

import mills.bits.PopCount;
import mills.index.PosIndex;
import mills.index1.R0Table;
import mills.index1.R2Entry;
import mills.index1.R2Table;
import mills.index1.partitions.LePopTable;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.09.2019 11:09
 * modified by: $
 * modified on: $
 */
public class IndexBuilder {

    final EntryTables tables;

    final LePopTable lePopTable;
    final LePopTable minPopTable;

    final List<EntryTable> partitions;

    final List<EntryTable[]> fragments = AbstractRandomList.generate(PopCount.SIZE, pop -> new EntryTable[128]);

    private IndexBuilder(EntryTables tables, LePopTable lePopTable, LePopTable minPopTable,
                         List<EntryTable> partitions) {
        this.tables = tables;
        this.lePopTable = lePopTable;
        this.minPopTable = minPopTable;
        this.partitions = partitions;
    }

    public static IndexBuilder create() {
        EntryTables tables = new EntryTables();
        LePopTable lePopTable = LePopTable.build(Entries.TABLE, tables::table);
        LePopTable minPopTable = LePopTable.build(Entries.MINIMIZED, tables::table);
        AbstractRandomList<EntryTable> partitions = AbstractRandomList.transform(PopCount.TABLE,
                pop->pop.sum()<=8 ? tables.table(Entries.TABLE.filter(pop.eq)) : EntryTable.EMPTY);
        return new IndexBuilder(tables, lePopTable, minPopTable, partitions.copyOf());
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
        T0Builder builder = getBuilder();
        try{
            return t0(builder, e2, pop);
        } finally {
            release(builder);
        }
    }

    private R0Table t0(T0Builder builder, RingEntry e2, PopCount pop) {
        PopCount p2 = pop.sub(e2.pop);

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

            int meq = meq(e2, e0);
            if(meq==0)
                continue;

            EntryTable tf = fragment(p1, t1,  meq);

            if(tf.isEmpty())
                continue;

            builder.add(e0, tf);
        }

        return builder.build();
    }

    EntryTable fragment(PopCount p1, EntryTable t1, int msk) {
        EntryTable[] ft = fragments.get(p1.index);

        EntryTable tf = ft[msk/2];

        if(tf==null) {
            tf = t1.filter(anyMLT(msk));
            tf = tables.table(tf);
            ft[msk/2] = tf;
        }

        return tf;
    }

    static Predicate<RingEntry> anyMLT(int msk) {
        return e -> (e.mlt&msk)==0;
    }

    /**
     * Return a perm mask of all stable permutations.
     * If any permutation reduces r20 return 0.
     * Else bit #0 is set.
     * @param e2 entry on ring 0 (minimized).
     * @param e0 entry on ring 2.
     * @return a perm mask of all stable permutations or 0.
     */
    static int meq(RingEntry e2, RingEntry e0) {

        int meq = e2.meq & 0xff;

        // no further analysis necessary.
        if(e2==e0)
            return meq;

        // may be reduced easily
        int mlt = meq & e0.mlt;
        if (mlt != 0) // unstable anyway
            return 0;

        // ether both are stable
        meq &= e0.meq;

        // no swap possible since e0 has an other (bigger) minimum
        if (e2.index != e0.min())
            return meq;

        // analyze all minima
        int min = e0.min & 0xff;
        while (min != 0) {
            int mi = Integer.lowestOneBit(min);
            min ^= mi;
            int i = Integer.numberOfTrailingZeros(mi);

            // even reduces
            if (e2.perm(i) < e0.index) {
                return 0;
            }

            // also stable with swap
            if (e2.perm(i) == e0.index) {
                meq |= mi;
            }
        }

        return meq;
    }

    class T0Builder {
        final List<RingEntry> t0 = new ArrayList<>();
        final List<EntryTable> t1 = new ArrayList<>();

        public void clear() {
            t0.clear();
            t1.clear();
        }

        void add(RingEntry e0, EntryTable t) {
            t0.add(e0);
            t1.add(t);
        }

        R0Table build() {
            return R0Table.of(EntryTable.of(t0), tables.build(t1));
        }
    }

    private final ConcurrentLinkedQueue<T0Builder> builders = new ConcurrentLinkedQueue<>();

    private T0Builder getBuilder() {
        T0Builder builder = builders.poll();
        if(builder==null)
            builder = new T0Builder();
        return builder;
    }

    private void release(T0Builder builder) {
        builder.clear();
        builders.offer(builder);
    }
}
