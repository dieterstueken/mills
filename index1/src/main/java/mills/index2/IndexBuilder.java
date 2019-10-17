package mills.index2;

import mills.bits.PopCount;
import mills.index1.C2Table;
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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
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

    final EntryTables registry;

    final LePopTable lePopTable;
    final LePopTable minPopTable;

    final Partitions partitions;

    final List<EntryTable[]> fragments = AbstractRandomList.generate(PopCount.SIZE, pop -> new EntryTable[128]);

    public IndexBuilder(LePopTable lePopTable, LePopTable minPopTable,
                        Partitions partitions, EntryTables registry) {
        this.lePopTable = lePopTable;
        this.minPopTable = minPopTable;
        this.partitions = partitions;
        this.registry = registry;
    }

    EntryTable table(EntryTable table) {
        return registry==null ? table : registry.table(table);
    }

    List<EntryTable> tables(List<EntryTable> tables) {
        return registry==null ? tables : registry.register(tables);
    }

    public static IndexBuilder create() {
        return create(new EntryTables());
    }

    public static IndexBuilder create(EntryTables registry) {
        LePopTable lePopTable = LePopTable.build(Entries.TABLE, registry::table);
        LePopTable minPopTable = LePopTable.build(Entries.MINIMIZED, registry::table);
        Partitions partitions = Partitions.build(Entries.TABLE, registry);

        return new IndexBuilder(lePopTable, minPopTable, partitions, registry);
    }

    public Map<PopCount, C2Table> buildGroup(PopCount pop) {
        return PopCount.CLOSED.parallelStream()
                .map(clop -> build(pop, clop))
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableMap(c2->c2.clop, Function.identity()));
    }

    public R2Table build(PopCount pop) {
        return build(pop, null);
    }

    public C2Table build(PopCount pop, PopCount clop) {

        List<R2Entry> table = minPopTable.get(pop).parallelStream()
                .map(e2 -> r2t0(e2, pop, clop))
                .filter(Objects::nonNull)
                .sorted(R2Entry.R2)
                .collect(Collectors.toList());

        if(table.isEmpty())
            return null;

        List<RingEntry> t2 = AbstractRandomList.transform(table, R2Entry::r2);
        List<R0Table> r0t = AbstractRandomList.transform(table, R2Entry::t0).copyOf();

        // todo normalize t2
        return C2Table.of(pop, clop, t2, r0t);
    }

    protected R2Entry r2t0(RingEntry e2, PopCount pop, PopCount clop) {
        R0Table t0 = t0(e2, pop, clop);
        return t0.isEmpty()? null : new R2Entry(e2, t0);
    }

    static final PopCount DEBUG = PopCount.of(2, 1);

    private R0Table t0(RingEntry e2, PopCount pop, PopCount clop) {
        PopCount pop2 = pop.sub(e2.pop);
        if(pop2==null)
            return R0Table.EMPTY;

        Function<RingEntry, Predicate<RingEntry>> milf = null;

        if(clop!=null) {
            // remaining closes
            PopCount clop2 = clop.sub(e2.clop());

            boolean debug = clop.equals(DEBUG);
            if(debug)
                e2.singleton();

            // too many
            if(clop2==null)
                return R0Table.EMPTY;
            
            // can be reached?
            if(pop2.mclop().add(e2.radials().pop).sub(clop2)==null)
                return R0Table.EMPTY;

            milf = e0 -> {
                // remaining closes necessary
                PopCount clop0 = clop2.sub(e0.clop());
                if(clop0==null)
                    return null;

                // are those reachable?

                RingEntry rad20 = e2.radials().and(e0.radials());

                return e1 -> {
                    if(debug)
                        e2.singleton();

                    // mills closed by e1
                    PopCount clop1 = rad20.and(e1.radials()).pop().add(e1.clop());
                    if(clop1.equals(clop0)) {
                        if(debug)
                            return true;
                        else
                            return true;
                    } else
                    if(debug)
                        return false;
                    else
                        return false;
                };
            };
        }

        T0Builder builder = getBuilder();
        try{
            return t0(builder, e2, pop2, milf);
        } finally {
            release(builder);
        }
    }

    private R0Table t0(T0Builder builder, RingEntry e2, PopCount pop2, Function<RingEntry, Predicate<RingEntry>> milf) {
        EntryTable lt0 = lePopTable.get(pop2);

        for (RingEntry e0 : lt0) {

            // e2 is minimized.
            // if e0 may be minimized to a smaller value they may be swapped.
            if(e0.min()>e2.index)
                continue;

            // no filter by default
            Predicate<RingEntry> f1 = null;

            if(milf!=null) {
                // if given this indicates an aboard
                f1 = milf.apply(e0);
                if (f1 == null)
                    continue;
            }
            
            // remaining PopCount of e1[]
            PopCount pop1 = pop2.sub(e0.pop);

            EntryTable t1 = partitions.get(pop1).root();
            if(t1.isEmpty())
                continue;

            int meq = meq(e2, e0);
            if(meq==0)
                continue;

            EntryTable tf = fragment(pop1, t1,  meq);

            // apply possible clop filter
            if(f1!=null)
                tf = tf.filter(f1);

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
            tf = table(tf);
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

        final List<RingEntry> t0 = new ArrayList<>(RingEntry.MAX_INDEX);
        final List<EntryTable> t1 = new ArrayList<>(RingEntry.MAX_INDEX);

        public void clear() {
            t0.clear();
            t1.clear();
        }

        void add(RingEntry e0, EntryTable t) {
            t0.add(e0);
            t = table(t);
            t1.add(t);
        }
        R0Table build() {
            List<EntryTable> n1 = tables(t1);
            return R0Table.of(EntryTable.of(t0), n1);
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
