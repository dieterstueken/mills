package mills.index3.partitions;

import mills.bits.*;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;
import mills.util.Stat;
import mills.util.Tasks;

import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:41
 * modified by: $Author$
 * modified on: $Date$
 */
public class Builder {

    protected final EntryTables registry;

    final AtomicInteger mt = new AtomicInteger();
    final AtomicInteger rt = new AtomicInteger();
    final AtomicInteger ct = new AtomicInteger();
    final AtomicInteger nc = new AtomicInteger();

    String stat = "";

    public String toString() {
        return String.format("Builder: %d %d %d %d", mt.get(), rt.get(), ct.get(), nc.get());
    }

    protected Builder(EntryTables registry) {
        this.registry = registry;
    }

    protected Partitions partitions() {

        List<MaskTable> partitions = Arrays.asList(new MaskTable[100]);
        Collections.fill(partitions, MaskTable.EMPTY);

        List<MaskTable> content = new ArrayList<MaskTable>(45) {
            @Override
            public boolean add(MaskTable t) {
                partitions.set(t.pop().index, t);
                return super.add(t);
            }
        };

        ForkJoinTask<List<EntryTable>> lePops = lePops().fork();

        Tasks.computeAll(PopCount.TABLE.stream()
                .filter(pop -> pop.sum()<=8), this::maskTable)
                .forEach(content::add);

        //System.out.format("radials: %d clops: %d\n", radials.get(), clops.get());


        return new Partitions(registry, partitions, content, lePops.join());
    }

    private ForkJoinTask<List<EntryTable>> lePops() {

        final List<EntryTable> tables = Arrays.asList(new EntryTable[PopCount.P88.index+1]);

        return new RecursiveTask<List<EntryTable>>() {

            @Override
            protected List<EntryTable> compute() {

                Deque<ForkJoinTask<?>> tasks = new ArrayDeque<>();

                EntryTable table = Entries.TABLE;
                List<RingEntry> entries = new ArrayList<>(table);
                set(PopCount.P88, table);

                for(int n=8; n>0; --n) {

                    tasks.addFirst(submit(table, n, Player.Black.pop));
                    tasks.addFirst(submit(table, n, Player.White.pop));

                    PopCount pop = PopCount.of(n-1,n-1);
                    entries.removeIf(pop.gt);
                    table = set(pop, entries);
                }

                tables.set(PopCount.EMPTY.index, Entries.of(0).singleton);

                tasks.stream().forEach(ForkJoinTask::join);

                List<EntryTable> t100 = AbstractRandomList.virtual(100, index -> {
                    PopCount pop = PopCount.get(index).min(PopCount.P88);
                    return tables.get(pop.index);
                });

                stat = Builder.this.toString();

                return registry.register(t100);
            }

            EntryTable set(PopCount pop, List<RingEntry> entries) {
                assert entries.stream().allMatch(pop.le);

                EntryTable table = registry.table(entries);

                Object o = tables.set(pop.index, table);

                if(o!=null)
                    throw new IllegalStateException("duplicate LePop");

                return table;
            }

            ForkJoinTask<?> submit(EntryTable root, int n, PopCount decr) {

                return new RecursiveAction() {

                    @Override
                    protected void compute() {
                        List<RingEntry> entries = new ArrayList<>(root);
                        PopCount pop = PopCount.of(n,n);
                        for(pop = pop.sub(decr); pop!=null; pop = pop.sub(decr)) {
                            entries.removeIf(pop.gt);
                            set(pop, entries);
                        }
                    }
                }.fork();
            }
        };
    }

    private EntryTable popTable(PopCount pop) {

        if(pop.sum()>8)
            return EntryTable.EMPTY;

        EntryTable table = Entries.MINIMIZED;
        table = table.filter(pop.eq);
        return registry.table(table);
    }

    private MaskTable maskTable(PopCount pop) {

        EntryTable root = popTable(pop);

        if(root.isEmpty())
            return MaskTable.EMPTY;

        final Set<PGroup> groups = PGroup.groups(root);

        final List<ForkJoinTask<RadialTable>> taskset = new ArrayList<>(1<<groups.size());

        final List<ForkJoinTask<RadialTable>> tasks = new ArrayList<>(128);

        IntConsumer build = new IntConsumer() {

            ForkJoinTask<RadialTable> task(int index) {
                ForkJoinTask<RadialTable> task = Tasks.submit(() -> build(index));
                taskset.add(task);
                return task;
            }

            @Override
            public void accept(int index) {
                int lindex = PGroup.lindex(groups, index);

                ForkJoinTask<RadialTable> task = lindex < index ? tasks.get(lindex) : task(index);
                tasks.add(index, task);
            }

            RadialTable build(int msk) {
                return new RadialBuilder(pop, root, msk).build();
            }
        };

        IntStream.range(0, 128).forEach(build);

        // invoke all and drop empty Radials
        Tasks.waitAll(taskset).removeIf(task -> task.join().root.isEmpty());

        assert !taskset.isEmpty();

        mt.incrementAndGet();

        return maskTable(pop, root, Tasks.joinAll(tasks), Tasks.joinAll(taskset));
    }

    static MaskTable maskTable(PopCount pop, EntryTable root, List<RadialTable> fragments, List<RadialTable> fragset) {

        return new MaskTable(root) {

            @Override
            public RadialTable get(int index) {
                return fragments.get(index);
            }

            @Override
            public List<RadialTable> content() {
                return fragset;
            }

            public PopCount pop() {
                return pop;
            }
        };
    }

    // create clip operator from given list of entries
    static Patterns clip(EntryTable table) {

        // a position may be clipped if
        // 1) no occupation of given color exists
        //      -> clip won't change clop count
        // 2) all colors are the same
        //      -> all clop counts are affected the same way

        Patterns any = new Patterns(Pattern.NONE, Pattern.NONE);
        Patterns all = new Patterns(Pattern.ALL, Pattern.ALL);

        for (RingEntry e : table) {
            e = e.radials(); // radials only

            any = any.or(e);
            all = all.and(e);
        }

        // any position remains except if all are the same
        return any.and(all.not());
        //return entry -> entry.and(clip);
    }

    class RadialBuilder {

        final String name;

        final EntryTable root;

        final List<ForkJoinTask<ClopTable>> taskset = new ArrayList<>();
        final List<ForkJoinTask<ClopTable>> tasks = new ArrayList<>(81);

        final Patterns clip;

        ForkJoinTask<ClopTable> task(RingEntry entry) {
            RingEntry radials = entry.and(clip);
            int radix = radials.radix();
            int size = tasks.size();
            if (radix < size) {
                return tasks.get(radix);
            } else {
                assert radix == size;
                ForkJoinTask<ClopTable> task = Tasks.submit(() -> clopTable(radials));
                taskset.add(task);
                return task;
            }
        }

        RadialBuilder(PopCount pop, EntryTable src, int msk) {
            this.root = src.filter(e->e.stable(2*msk));
            this.clip = Builder.clip(root);

            this.name = String.format("%s %d", pop, msk);
        }

        public RadialTable build() {
            taskset.clear();
            tasks.clear();

            Entries.RADIALS.stream()
                    .map(this::task)
                    .forEach(tasks::add);

            assert !taskset.isEmpty();
            Tasks.waitAll(taskset);

            List<ClopTable> fragset = Tasks.joinAll(taskset);
            List<ClopTable> tables = Tasks.joinAll(tasks);

            rt.incrementAndGet();
            return radialTable(root, tables, fragset);
        }

        ClopTable clopTable(RingEntry radials) {

            if(root.isEmpty())
                return ClopTable.EMPTY;

            Map<PopCount, List<RingEntry>> clops = new TreeMap<>();

            for (RingEntry e: root) {
                PopCount clop = radials.and(e.radials()).pop().add(e.clop());

                // drop excessive clops as it will even grow with additional radials
                //if(clop.max()<=4)
                    clops.computeIfAbsent(clop, c -> new ArrayList()).add(e);
            }

            assert !clops.isEmpty();

            ct.incrementAndGet();
            nc.addAndGet(clops.size());

            String parent = name;

            return new ClopTable(registry.register(clops.values())) {
                @Override
                public String toString() {
                    return parent;
                }
            };
        }
    }

    static RadialTable radialTable(EntryTable root, List<ClopTable> tables, Collection<ClopTable> fragset) {

        //assert verify(tables);

        return new RadialTable(root) {

            @Override
            public ClopTable get(int index) {
                return tables.get(index);
            }

            @Override
            public Collection<ClopTable> content() {
                return fragset;
            }
        };
    }

    static boolean verify(List<ClopTable> table) {

        for (int i = 0; i < table.size(); i++) {
            ClopTable clops = table.get(i);
            RingEntry rad = Entries.RADIALS.get(i);
            Set<PopCount> cset = new HashSet<>();
            for (EntryTable t0 : clops.t0) {
                RingEntry e0 = t0.get(0);
                PopCount p = e0.radials().and(rad).pop();
                p = p.add(e0.clop());

                if(!cset.add(p))
                    return false;
            }
        }
        return true;
    };

    public static void main(String ... args) {

        Builder b = new Builder(new EntryTables());

        Partitions p = b.partitions();

        System.out.println(b.toString());
        System.out.println(b.stat);

        int n_rads=0, n_msk=0, n_clops=0, n_tables=0;

        Stat nclopt = new Stat();
        Stat nclops = new Stat();

        for (MaskTable partition : p.content()) {

            if(!partition.content().isEmpty())
                ++n_msk;

            for (RadialTable radialTable : partition.content()) {
                if(!radialTable.content().isEmpty())
                    ++n_rads;

                nclopt.accept(radialTable.content().size());

                for (ClopTable clopTable : radialTable.content()) {
                    if(!clopTable.isEmpty())
                        ++n_clops;

                    nclops.accept(clopTable.size());

                    n_tables += clopTable.size();
                }
            }
        }

        //b.registry.stat(System.out);
        System.out.format("run msk=%d rad=%d clop=%d tbl=%d all=%d\n", n_msk, n_rads, n_clops, n_tables, b.registry.count());

        nclops.dump("nclops");
        nclopt.dump("nclopt");

        p.size();
    }
}
