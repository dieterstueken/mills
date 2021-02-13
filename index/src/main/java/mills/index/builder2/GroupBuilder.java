package mills.index.builder2;

import mills.bits.PopCount;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.*;
import mills.util.AbstractRandomList;
import mills.util.ArraySet;
import mills.util.ListSet;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.function.Predicate.not;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 09:04
 */
public class GroupBuilder {

    final Partitions partitions;

    final PopMap<EntryTable> lePops;
    final PopMap<EntryTable> minPops;

    GroupBuilder(Supplier<Partitions> partitions) {
        this.lePops = PopMap.lePops(Entries.TABLE);
        this.minPops = PopMap.lePops(Entries.MINIMIZED);
        this.partitions = partitions.get();
    }

    public static GroupBuilder create() {
        var task = ForkJoinTask.adapt(Partitions::new).fork();
        return new GroupBuilder(task::join);
    }

    public Map<PopCount, C2Table> buildGroup(PopCount pop) {
        return new Builder(pop).build();
    }

    class Builder {

        final PopCount pop;

        final EntryTable t2;

        final ListSet<PopCount> clops;

        final PopMap<C2Builder> builders = PopMap.allocate(PopCount.NCLOPS);

        Builder(PopCount pop) {
            this.pop = pop;
            this.t2 = minPops.get(pop);
            final PopCount mclop = pop.mclop(false);
            this.clops = ListSet.of(PopCount.CLOPS.stream()
                    .filter(mclop::ge)
                    .collect(Collectors.toUnmodifiableList()));

            // preset possible builders
            clops.forEach(clop -> builders.put(clop, new C2Builder(clop)));
        }

        C2Builder getC2Builder(PopCount clop) {
            return builders.get(clop);
        }

        Map<PopCount, C2Table> build() {

            t2.parallelStream().forEach(this::build);

            clops.parallelStream().map(this::getC2Builder).forEach(C2Builder::build);

            var c2Table = clops.stream()
                    .map(this::getC2Builder)
                    .map(C2Builder::build)
                    .collect(Collectors.toUnmodifiableList());

            return ArraySet.mapOf(c2Table, C2Table::clop, null);
        }

        class C2Builder {

            final PopCount clop;

            final EntryMap<R0Table> t0Tables;

            transient C2Table result;

            C2Builder(PopCount clop) {
                this.clop = clop;
                this.t0Tables = EntryMap.preset(t2, R0Table.EMPTY);
            }

            void put(RingEntry r2, R0Table t0Table) {
                t0Tables.put(r2, t0Table);
            }

            C2Table build() {
                if(result!=null)
                    return result;

                var filtered= t0Tables.filterValues(not(R0Table::isEmpty));
                return result = C2Table.of(pop, clop, filtered);
            }
        }

        void build(RingEntry r2) {
            final T0Builders builder = getBuilder();
            try {
                builder.build(r2);
            } finally {
                release(builder);
            }
        }

        private final ConcurrentLinkedQueue<T0Builders> cache = new ConcurrentLinkedQueue<>();

        T0Builders getBuilder() {
            T0Builders builders = cache.poll();

            if(builders==null)
                builders = new T0Builders();

            return builders;
        }

        void release(T0Builders builders) {
            //cache.offer(builders);
        }

        class T0Builders {

            final PopMap<T0Builder> builders = PopMap.allocate(PopCount.NCLOPS);

            T0Builders() {
                clops.forEach(clop -> builders.put(clop, new T0Builder(clop)));
            }

            void build(RingEntry r2) {
                PopCount pop0 = pop.sub(r2.pop);
                EntryTable t0 = lePops.get(pop0).tailSet(r2);
                if(t0.isEmpty())
                    return;

                clops.forEach(clop -> builders.get(clop).setup(t0));

                IntStream.range(0, t0.size()).parallel().forEach(i0 -> process(r2, t0, i0, pop0));

                clops.parallelStream()
                        .map(builders::get)
                        .forEach(builder -> {
                            R0Table result = builder.build(pop0, t0.size());
                            getC2Builder(builder.clop).put(r2, result);
                        });
            }

            void process(RingEntry r2, EntryTable t0, int i0, PopCount pop0) {
                RingEntry r0 = t0.get(i0);
                PopCount pop1 = pop0.sub(r0.pop);

                int meq2 = Positions.meq(r2, r0);
                if (meq2 != 0) {
                    RingEntry rad = r2.radials().and(r0);
                    PopCount clop20 = r2.clop().add(r0.clop());
                    partitions.get(pop1).getFragments(meq2).get(rad).forEach(t1 -> {
                        PopCount clops = t1.get(0).clop(rad).add(clop20);
                        T0Builder builder = builders.get(clops);
                        if(builder!=null)
                            builder.put(i0, t1);
                    });
                }
            }

            class T0Builder {

                final PopCount clop;

                int size;
                int[] table;

                boolean empty = true;

                T0Builder(PopCount clop) {
                    this.clop = clop;
                }

                RingEntry r0(int index) {
                    return Entries.of(table[index] >> 16);
                }

                short i1(int index) {
                    return (short)(table[index] & 0xffff);
                }

                final List<RingEntry> l0 = new AbstractRandomList<>() {
                    @Override
                    public int size() {
                        return size;
                    }

                    @Override
                    public RingEntry get(int index) {
                        return r0(index);
                    }
                };

                void setup(EntryTable t0) {
                    int size = t0.size();

                    if(table==null || table.length<size)
                        table = new int[size];

                    for (int i = 0; i < size; i++) {
                        RingEntry r0 = t0.get(i);
                        table[i] = (r0.index << 16) + 0xffff;
                    }

                    empty = true;

                    this.size = size;
                }

                void put(int i0, IndexedEntryTable t1) {
                    int value = table[i0];
                    value &= 0xffff0000;
                    value |= t1.getIndex() & 0xffff;
                    table[i0] = value;
                    empty = false;
                }

                R0Table build(PopCount pop0, int n0) {

                    // compact relevant entries
                    size = 0;

                    if(!empty)
                    for(int i=0; i<n0; ++i) {
                        if(i1(i)>=0) {
                            if(i>size)
                                table[size] = table[i];
                            ++size;
                        }
                    }

                    if(size==0)
                        return R0Table.EMPTY;

                    if(size==1) {
                        RingEntry r0 = r0(0);
                        int key = i1(0);
                        PopCount pop1 = pop0.sub(r0.pop);
                        IndexedEntryTable t1 = partitions.get(pop1).tables.get(key);

                        return R0Table.of(r0.singleton, List.of(t1));
                    }

                    // reduced t0
                    EntryTable t0 = EntryTable.of(l0);

                    short[] s1 = new short[size];
                    for(int i=0; i<size; ++i) {
                        s1[i] = i1(i);
                    }

                    return r0Table(partitions, t0, pop0, s1);
                }
            }
        }
    }

    static R0Table r0Table(Partitions partitions, EntryTable t0, PopCount pop0, short[] indexes) {

        List<EntryTable> t1 = new AbstractRandomList<>() {

            @Override
            public int size() {
                return indexes.length;
            }

            @Override
            public IndexedEntryTable get(int index) {
                RingEntry r0 = t0.get(index);
                PopCount pop1 = pop0.sub(r0.pop);
                int key = indexes[index];
                return partitions.get(pop1).tables.get(key);
            }
        };

        //try {
        //    List.copyOf(t1);
        //} catch(Throwable error) {
        //    error.printStackTrace();
        //}

        return R0Table.of(t0, t1);
    }

    public static void main(String ... args) {

        PopCount pop = PopCount.of(8,9);

        //PopCount.TABLE.forEach(pop ->
        {
            GroupBuilder groupBuilder = timer("init", GroupBuilder::create);

            var group = timer("groups", () -> groupBuilder.buildGroup(pop));

            System.out.format("%s groups: %d\n", pop, group.size());

            group.forEach((clop, c2t) -> {
                System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
            });

            System.out.println();

            /*
            IndexBuilder indexBuilder = new IndexBuilder();

            var indexGroup = timer("index", () -> indexBuilder.buildGroup(pop));

            System.out.format("%s groups: %d\n", pop, indexGroup.size());

            indexGroup.forEach((clop, c2t) -> {
                System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
            });

            System.out.println();
            */
        }
    }

    static <T> T timer(String name, Supplier<T> proc) {
        double start = System.currentTimeMillis();
        T t = proc.get();
        double stop = System.currentTimeMillis();

        System.out.format("%s: %.3fs\n", name, (stop - start) / 1000);

        return t;
    }

}
