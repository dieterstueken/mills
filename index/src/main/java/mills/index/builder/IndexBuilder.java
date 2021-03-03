package mills.index.builder;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.index.tables.R2Entry;
import mills.index.tables.R2Table;
import mills.position.Positions;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.stream.Collectors;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  18.09.2019 11:09
 * modified by: $
 * modified on: $
 */
public class IndexBuilder implements IndexProvider {

    final EntryTables registry;

    final PopTable lePopTable;
    final PopTable minPopTable;

    final Map<PopCount, Partition> partitions;

    public IndexBuilder(PopTable lePopTable, PopTable minPopTable,
                        Map<PopCount, Partition> partitions, EntryTables registry) {
        this.lePopTable = lePopTable;
        this.minPopTable = minPopTable;
        this.partitions = partitions;
        this.registry = registry;
    }

    public IndexBuilder(EntryTables registry) {
        ForkJoinTask<PopTable> lePopTable = PopTable.fork(Entries.TABLE, registry::table);
        ForkJoinTask<PopTable> minPopTable = PopTable.fork(Entries.MINIMIZED, registry::table);
        Map<PopCount, Partition> partitions = Partition.partitions(Entries.TABLE, registry);
        this.lePopTable = lePopTable.join();
        this.minPopTable = minPopTable.join();
        this.partitions = partitions;
        this.registry = registry;
    }

    public IndexBuilder() {
        this(new EntryTables());
    }

    EntryTable table(EntryTable table) {
        return registry==null ? table : registry.table(table);
    }

    List<EntryTable> tables(List<EntryTable> tables) {
        return registry==null ? tables : registry.register(tables);
    }

    public static IndexBuilder create() {
        return new IndexBuilder();
    }

    public static IndexBuilder create(EntryTables registry) {
        return new IndexBuilder(registry);
    }

    public CachedBuilder cached() {
        return new CachedBuilder(this);
    }

    public Map<PopCount, C2Table> buildGroup(PopCount pop) {

        C2Table[] tables = new C2Table[PopCount.CLOPS.size()];

        PopCount.CLOPS.parallelStream().forEach(clop -> tables[clop.index] = this.build(pop, clop));

        Map<PopCount, C2Table> group = new TreeMap<>();

        for (C2Table table : tables) {
            if(table!=null)
                group.put(table.clop, table);
        }

        return group;
    }

    public R2Table build(PopCount pop) {
        return this.build(pop, null);
    }

    public C2Table build(Clops clops) {
        return this.build(clops.pop(), clops.clop());
    }

    public C2Table build(PopCount pop, PopCount clop) {

        List<R2Entry> table = minPopTable.get(pop).parallelStream()
                .map(e2 -> r2t0(e2, pop, clop))
                .filter(Objects::nonNull)
                .sorted(R2Entry.R2)
                .collect(Collectors.toList());

        if(table.isEmpty())
            return null;

        List<R0Table> r0t = AbstractRandomList.transform(table, R2Entry::t0).copyOf();
        List<RingEntry> t2 = AbstractRandomList.transform(table, R2Entry::r2);
        t2 = registry.table(t2);

        return C2Table.of(pop, clop, t2, r0t);
    }

    private R2Entry r2t0(RingEntry e2, PopCount pop, PopCount clop) {
        R0Table t0 = t0(e2, pop, clop);
        return t0.isEmpty()? null : new R2Entry(e2, t0);
    }

    private R0Table t0(RingEntry e2, PopCount pop, PopCount clop) {
        PopCount pop2 = pop.sub(e2.pop);
        if(pop2==null)
            return R0Table.EMPTY;
        
        if(clop!=null) {
            // remaining closes
            PopCount clop2 = clop.sub(e2.clop());

            // too many
            if(clop2==null)
                return R0Table.EMPTY;
            
            // can be reached?
            if(pop2.mclop().add(e2.radials().pop).sub(clop2)==null)
                return R0Table.EMPTY;
        }

        T0Builder builder = getBuilder();
        try{
            return t0(builder, e2, pop2, clop);
        } finally {
            release(builder);
        }
    }

    private R0Table t0(T0Builder builder, RingEntry e2, PopCount pop2, PopCount clop) {
        EntryTable lt0 = lePopTable.get(pop2);
        PopCount clop2 = clop==null ? null : clop.sub(e2.clop());

        for (RingEntry e0 : lt0) {

            // e2 is minimized.
            // if e0 can be minimized to a smaller value then the swapped value becomes smaller.
            if(e0.min()<e2.index)
                continue;

            // remaining PopCount of e1[]
            PopCount pop1 = pop2.sub(e0.pop);

            Partition partition = partitions.get(pop1);

            EntryTable t1 = partition.root();
            if(t1.isEmpty())
                continue;

            // no filter by default
            PopCount clop1 = null;

            if(clop2!=null) {
                clop1 = clop2.sub(e0.clop());

                if(clop1==null)
                    continue;

                if(clop1.max()>4)
                    continue;
            }

            int meq = Positions.meq(e2, e0);
            if(meq==0)
                continue;

            Fragments fragments = partition.get(meq);
            EntryTable tf = fragments.root();

            // apply possible clop filter
            if(clop1!=null) {
                RingEntry rad20 = e2.and(e0).radials();
                tf = fragments.get(clop1, rad20);
            }

            if(tf.isEmpty())
                continue;

            builder.add(e0, tf);
        }

        return builder.build();
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
