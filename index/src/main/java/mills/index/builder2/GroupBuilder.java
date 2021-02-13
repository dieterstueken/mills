package mills.index.builder2;

import mills.bits.PopCount;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.*;
import mills.util.AbstractRandomArray;
import mills.util.ArraySet;
import mills.util.ListSet;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
            PopCount pop0 = pop.sub(r2.pop);

            if(pop0.sum()>16)
                return;

            EntryTable t0 = lePops.get(pop0).tailSet(r2);

            T0Builder builder = getBuilder();
            try {
                builder.build(r2, pop0, t0);
            } finally {
                builder.clear();
                release(builder);
            }
        }

        private final ConcurrentLinkedQueue<T0Builder> cache = new ConcurrentLinkedQueue<>();

        T0Builder getBuilder() {
            T0Builder builders = cache.poll();

            if(builders==null)
                builders = new T0Builder();

            return builders;
        }

        void release(T0Builder builder) {
            cache.offer(builder);
        }

        class T0Builder {

            static final int S = 14;

            int[] table;
            int size = 0;
            int maxSize = 0;

            int size() {
                return size;
            }

            void clear() {
                if(size>maxSize)
                    maxSize = size;
                
                size = 0;
            }

            short getKey(int index) {
                int pattern = table[index];
                return (short)(pattern&((1<<S)-1));
            }

            RingEntry getEntry(int index) {
                int pattern = table[index];
                int i1 = (pattern>>>S)%RingEntry.MAX_INDEX;
                return Entries.of(i1);
            }

            int getClopIndex(int index) {
                int pattern = table[index];
                return (pattern>>>S)/RingEntry.MAX_INDEX;
            }

            void add(int index) {
                if(table==null)
                    table = new int[1024];
                else
                if(size >= table.length)
                    table = Arrays.copyOf(table, table.length*2);

                table[size++] = index;
            }

            void add(PopCount clop, RingEntry r0, IndexedEntryTable t1) {
                int index = t1.getIndex();
                if(index>=(1<<S))
                    throw new IndexOutOfBoundsException("IndexedEntryTable to big");

                index += (r0.index + RingEntry.MAX_INDEX*clop.index) << S;
               
                add(index);
            }

            void build(RingEntry r2, PopCount pop0, EntryTable t0) {
                clear();

                if(t0.isEmpty())
                    return;

                for (RingEntry r0 : t0) {
                    PopCount pop1 = pop0.sub(r0.pop);
                    final Partition partition = partitions.get(pop1);

                    if(r0.pop().ge(PopCount.of(4,5)))
                        r0.pop();

                    if(partition.isEmpty())
                        continue;

                    int meq2 = Positions.meq(r2, r0);
                    if (meq2 == 0)
                        continue;

                    RingEntry rad = r2.radials().and(r0);
                    PopCount clop20 = r2.clop().add(r0.clop());

                    final List<IndexedEntryTable> t1tables = partition.getFragments(meq2).get(rad);
                    for (IndexedEntryTable t1 : t1tables) {
                        PopCount clop = t1.get(0).clop(rad).add(clop20);
                        add(clop, r0, t1);
                    }
                }

                if(size==0)
                    return;

                Arrays.sort(table, 0, size);
                add(-1); // terminal dummy index

                int offt=0;
                int clop=getClopIndex(0);

                for(int i=1; i<size; ++i) {
                    int iclop = getClopIndex(i);
                    if(iclop!=clop) {
                        C2Builder c2Builder = getC2Builder(PopCount.get(clop));
                        if(c2Builder!=null) {
                            R0Table r0t = build(pop0, offt, i - offt);
                            c2Builder.put(r2, r0t);
                        }
                        offt = i;
                        clop = iclop;
                    }
                }

                clear();
            }

            private R0Table build(PopCount pop0, int offt, int size) {

                if(size==0)
                    return R0Table.EMPTY;

                if(size==1) {
                    RingEntry r0 = getEntry(offt);
                    int key = getKey(offt);
                    PopCount pop1 = pop0.sub(r0.pop);
                    IndexedEntryTable t1 = partitions.get(pop1).tables.get(key);
                    return R0Table.of(r0.singleton, List.of(t1));
                }

                EntryTable t0 = EntryTable.of(new AbstractRandomArray<>(size) {
                    @Override
                    public RingEntry get(int index) {
                        return getEntry(index+offt);
                    }
                });

                short[] s1 = new short[size];
                for(int index=0; index<size; ++index) {
                    s1[index] = getKey(index+offt);
                }

                List<EntryTable> t1 = new AbstractRandomArray<>(size) {
                    @Override
                    public IndexedEntryTable get(int index) {
                        RingEntry r0 = t0.get(index);
                        PopCount pop1 = pop0.sub(r0.pop);
                        int key = s1[index];
                        return partitions.get(pop1).tables.get(key);
                    }
                };
                
                return R0Table.of(t0, t1);
            }
        }
    }

    public static void main(String ... args) {

        GroupBuilder groupBuilder = timer("init", GroupBuilder::create);

        timer("groups", () -> {
            Set<Object> groups = new HashSet<>();

            PopCount.TABLE.forEach(pop -> {

                var group = groupBuilder.buildGroup(pop);
                groups.add(group);

                System.out.format("%s groups: %d\n", pop, group.size());

                group.forEach((clop, c2t) -> {
                    System.out.format("%s: %4d %,13d\n", clop.toString(), c2t.n20(), c2t.range());
                });

                System.out.println();
            });

            return null;
        });
    }

    static <T> T timer(String name, Supplier<T> proc) {
        double start = System.currentTimeMillis();
        T t = proc.get();
        double stop = System.currentTimeMillis();

        System.out.format("%s: %.3fs\n", name, (stop - start) / 1000);

        return t;
    }

}
