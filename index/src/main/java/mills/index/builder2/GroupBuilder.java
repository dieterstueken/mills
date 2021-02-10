package mills.index.builder2;

import mills.bits.PopCount;
import mills.index.builder.IndexBuilder;
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
import java.util.stream.Collectors;

import static java.util.function.Predicate.not;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 09:04
 */
public class GroupBuilder {

    final Partitions partitions = new Partitions();

    final PopMap<EntryTable> lePops = PopMap.lePops(Entries.TABLE);
    final PopMap<EntryTable> minPops = PopMap.lePops(Entries.MINIMIZED);

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
                t0Tables = EntryMap.preset(t2, null);
            }

            void put(RingEntry r2, R0Table t0Table) {
                t0Tables.put(r2, t0Table);
            }

            C2Table build() {
                if(result!=null)
                    return result;

                if(PopCount.P44.equals(clop))
                    result = null;

                var filtered= t0Tables.filterValues(not(R0Table::isEmpty));
                return result = C2Table.of(pop, clop, filtered);
            }
        }

        void build(RingEntry r2) {
            getBuilder().build(r2);
        }

        private final ConcurrentLinkedQueue<T0Builders> cache = new ConcurrentLinkedQueue<>();

        T0Builders getBuilder() {
            T0Builders builders = cache.poll();
            
            if(builders==null)
                builders = new T0Builders();

            return builders;
        }

        void release(T0Builders builders) {
            cache.offer(builders);
        }

        class T0Builders {

            final PopMap<T0Builder> builders = PopMap.allocate(PopCount.NCLOPS);

            T0Builders() {
                clops.forEach(clop -> builders.put(clop, new T0Builder(clop)));
            }

            void build(RingEntry r2) {
                PopCount pop0 = pop.sub(r2.pop);
                EntryTable t0 = lePops.get(pop0).tailSet(r2);

                t0.parallelStream().forEach(r0->process(r2, r0, pop0.sub(r0.pop)));

                clops.parallelStream()
                        .map(builders::get)
                        .forEach(builder -> {
                            R0Table result = builder.build(t0, pop0);
                            getC2Builder(builder.clop).put(r2, result);
                        });

                release(this);
            }

            void process(RingEntry r2, RingEntry r0, PopCount pop1) {

                int meq2 = Positions.meq(r2, r0);
                if (meq2 != 0) {
                    RingEntry rad = r2.radials().and(r0);
                    PopCount clop20 = r2.clop().add(r0.clop());
                    partitions.get(pop1).getFragments(meq2).get(rad).forEach(t1 -> {
                        PopCount clops = t1.get(0).clop(rad).add(clop20);
                        T0Builder builder = builders.get(clops);
                        if(builder!=null)
                            builder.put(r0, t1);
                    });
                }
            }

            class T0Builder {

                final PopCount clop;

                T0Builder(PopCount clop) {
                    this.clop = clop;
                }

                final IndexedEntryTable[] t1Table = new IndexedEntryTable[RingEntry.MAX_INDEX];

                IndexedEntryTable get(RingEntry r0) {
                    return t1Table[r0.index];
                }

                IndexedEntryTable pop(RingEntry r0) {
                    int index = r0.index;
                    IndexedEntryTable table = t1Table[index];
                    t1Table[index] = null;
                    return table;
                }

                void put(RingEntry r0, IndexedEntryTable t1) {
                    t1Table[r0.index] = t1;
                }

                boolean isEmpty(RingEntry r0) {
                    IndexedEntryTable t1 = get(r0);
                    return t1==null || t1.isEmpty();
                }

                R0Table build(EntryTable t0, PopCount pop0) {

                    // filter relevant entries.
                    t0 = t0.filter(not(this::isEmpty));
                    int size = t0.size();

                    if(size==0)
                        return R0Table.EMPTY;

                    if(size==1) {
                        RingEntry r0 = t0.get(0);
                        IndexedEntryTable t1 = pop(r0);
                        return R0Table.of(t0, List.of(t1));
                    }

                    short[] indexes = new short[size];
                    for (int i = 0; i < size; i++) {
                        RingEntry r0 = t0.get(i);
                        IndexedEntryTable value = pop(r0);
                        indexes[i] = (short) value.getIndex();
                    }

                    return r0Table(partitions, t0, pop0, indexes);
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

        try {
            List.copyOf(t1);
        } catch(Throwable error) {
            error.printStackTrace();
        }

        return R0Table.of(t0, t1);
    }

    public static void main(String ... args) {

        GroupBuilder groupBuilder = new GroupBuilder();
        IndexBuilder indexBuilder = new IndexBuilder();

        //PopCount.TABLE.forEach(pop ->
        {
            PopCount pop = PopCount.of(8,9);

            timer("groups", () -> {

                        var group = groupBuilder.buildGroup(pop);

                        System.out.format("%s groups: %d\n", pop, group.size());

                        group.forEach((clop, c2t) -> {
                            System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                        });

                        System.out.println();
                    });

            timer("index", () -> {

                var indexGroup = indexBuilder.buildGroup(pop);

                System.out.format("%s groups: %d\n", pop, indexGroup.size());

                indexGroup.forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });

                System.out.println();
            });
        }
    }

    static void timer(String name, Runnable proc) {
        double start = System.currentTimeMillis();
        proc.run();
        double stop = System.currentTimeMillis();

        System.out.format("\n%.3fs\n", (stop - start) / 1000);
    }

}
