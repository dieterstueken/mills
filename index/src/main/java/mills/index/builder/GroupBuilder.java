package mills.index.builder;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.*;
import mills.util.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static java.util.function.Predicate.not;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 09:04
 */
public class GroupBuilder extends AbstractGroupBuilder {

    public interface Debug {
        default void start(PopCount pop) {}
        default void done(Group result) {};
    }

    static final Debug NOOP = new Debug() {};

    public class Entry extends CachedBuilder<Group> {

        final PopCount pop;

        public Entry(final PopCount p_pop) {
            pop = p_pop;
        }

        public PopCount pop() {
            return pop;
        }

        @Override
        protected Group build() {
            return newGroup(pop);
        }
    }

    public static GroupBuilder create() {
        return new GroupBuilder();
    }

    final Debug debug;

    final List<Entry> groups;

    public GroupBuilder(Debug debug) {
        super();

        this.debug = debug;
        this.groups = PopCount.TABLE.transform(this::newEntry).copyOf();
    }

    public GroupBuilder() {
        this(NOOP);
    }

    public Group group(PopCount pop) {
        return groups.get(pop.index).get();
    }

    public List<Entry> entries() {
        return groups;
    }

    private Entry newEntry(PopCount pop) {
        return new Entry(pop);
    }

    private Group newGroup(PopCount pop) {
        debug.start(pop);
        Group group = new Group(pop);
        debug.done(group);
        return group;
    }

    @Override
    public Map<PopCount, C2Table> buildGroup(PopCount pop) {
        return group(pop).group;
    }

    @Override
    public PosIndex build(PopCount pop, PopCount clop) {
        return group(pop).getIndex(clop);
    }

    public void close() {
        groups.forEach(CachedBuilder::clear);
    }

    public class Group implements PosIndex {

        final PopCount pop;

        public final ListMap<PopCount, C2Table> group;

        final IndexTable it;

        public Group(PopCount pop) {
            this.pop = pop;
            this.group = new Builder(pop).build(C2T::new);
            this.it = IndexTable.sum(group.values(), C2Table::range);
        }

        /**
         * A non-static implementation to hold a reference to its group.
         */
        class C2T extends C2Table {
            public C2T(PopCount clop, EntryMap<R0Table> r0Tables) {
                super(pop, clop, r0Tables.keySet(), r0Tables.values());
            }
        }

        public PosIndex getIndex(PopCount clop) {
            return clop==null ? this : group.get(clop);
        }

        @Override
        public PopCount pop() {
            return pop;
        }

        @Override
        public int range() {
            return it.range();
        }

        public int posIndex(long i201) {

            if(!Positions.normalized(i201))
                i201 = normalize(i201);
            else
                assert i201 == normalize(i201);

            PopCount clop = Positions.clop(i201);
            int pos = group.keySet().indexOf(clop);
            int posIndex = group.getValue(pos).posIndex(i201);
            int baseIndex = it.baseIndex(pos);

            // if missing return lower bound by negative index
            if(posIndex<0)
                posIndex -= baseIndex;
            else
                posIndex += baseIndex;

            return posIndex;
        }

        public long i201(int posIndex) {
            int pos = it.indexOf(posIndex);
            posIndex -= it.baseIndex(pos);
            C2Table c2t = group.getValue(pos);
            return c2t.i201(posIndex);
        }

        public IndexProcessor process(IndexProcessor processor, int start, int end) {

            for(int pos = it.indexOf(start); pos<group.size(); ++pos) {
                int baseIndex = it.baseIndex(pos);
                if(end<baseIndex)
                    break;

                C2Table c2t = group.getValue(pos);
                int pstart = Math.max(0, start-baseIndex);
                c2t.process(processor, pstart, end-baseIndex);
            }

            return processor;
        }

        @Override
        public int n20() {
            int n20 = 0;
            for(int pos=0; pos<group.size(); ++pos)
                n20 += group.getValue(pos).n20();
            return n20;
        }
    }

    class Builder {

        final PopCount pop;

        final EntryTable t2;

        // next r2 to build
        final AtomicInteger i2 = new AtomicInteger();

        int count = 0;

        final PopMap<C2Builder> builders = PopMap.allocate(PopCount.NCLOPS);

        boolean debug(RingEntry r0, RingEntry r2) {
            //if(!pop.equals(PopCount.P88))
            //    return false;
            //
            //if(r2.index==6520 && r0.index==6520)
            //    return true;
            //else
                return false;
        }

        Builder(PopCount pop) {
            this.pop = pop;
            this.t2 = minPops.get(pop);
        }

        private void setupBuilder(PopCount clop) {
            builders.put(clop, new C2Builder(clop));
        }

        <R extends Clops> ListMap<PopCount, R> build(BiFunction<PopCount, EntryMap<R0Table>, R> generator) {

            // subset of clops to build
            final PopCount mclop = pop.mclop(false);
            ListSet<PopCount> clops = ListSet.of(PopCount.CLOPS.stream()
                    .filter(mclop::ge).toList());

            // preset possible builders
            clops.forEach(this::setupBuilder);

            T0Builder builder = new T0Builder();
            builder.invoke();

            List<R> tmp = AbstractRandomList.preset(PopCount.NCLOPS, null);

            clops.parallelStream()
                    .map(builders::get)
                    .map(b->b.build(generator))
                    .forEach(result->tmp.set(result.clop().index, result));

            List<R> results = clops.transform(c -> tmp.get(c.index)).copyOf();

            return clops.mapOf(results);
        }

        RingEntry next() {
            int n2 = i2.getAndIncrement();

            if(n2<t2.size())
                return t2.get(n2);

            return null;
        }

        class C2Builder {

            final PopCount clop;

            final EntryMap<R0Table> t0Tables;

            C2Builder(PopCount clop) {
                this.clop = clop;
                this.t0Tables = EntryMap.preset(t2, R0Table.EMPTY);
            }

            void put(RingEntry r2, R0Table t0Table) {
                t0Tables.put(r2, t0Table);
            }

            <Result> Result build(BiFunction<PopCount, EntryMap<R0Table>, Result> group) {
                var filtered= t0Tables.filterValues(not(R0Table::isEmpty));
                return group.apply(clop, filtered);
            }
        }

        class T0Builder extends RecursiveAction {

            static final int M=12*1024;

            T0Builder() {
                ++count;
            }

            @Override
            protected void compute() {

                if(i2.get()>=t2.size())
                    return;

                T0Builder concurrent = new T0Builder();
                concurrent.fork();

                for(RingEntry r2 = next(); r2!=null; r2=next()) {
                    build(r2);
                }

                if(!concurrent.tryUnfork())
                    concurrent.join();
            }

            int[] table;
            int size = 0;

            short getKey(int index) {
                int pattern = table[index];
                return (short)(pattern%M);
            }

            RingEntry getEntry(int index) {
                int pattern = table[index];
                int i1 = (pattern/M)%RingEntry.MAX_INDEX;
                return Entries.of(i1);
            }

            int getClopIndex(int index) {
                int pattern = table[index];
                if(pattern<0)
                    return -1;
                return (pattern/M)/RingEntry.MAX_INDEX;
            }

            void setup(int n0) {
                size = 0;
                //if(table==null) {
                //    PopCount mclop = pop.mclop();
                //    n0 *= mclop.nb * mclop.nw + 1;
                //    n0 = Math.max(n0, 1024);
                //    table = new int[n0];
                //}
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
                if(index>=M)
                    throw new IndexOutOfBoundsException("IndexedEntryTable to big");

                index += (r0.index + RingEntry.MAX_INDEX*clop.index) * M;
                if(index<0)
                    throw new IndexOutOfBoundsException("IndexedEntryTable negative index");

                add(index);
            }

            void build(RingEntry r2) {

                PopCount pop0 = pop.sub(r2.pop);

                if(pop0.sum()>16)
                    return; // won't fit into two rings

                EntryTable t0 = lePops.get(pop0).tailSet(r2);

                if(t0.isEmpty())
                    return;

                setup(t0.size());

                for (RingEntry r0 : t0) {

                    // debug(r0, r2);

                    PopCount pop1 = pop0.sub(r0.pop);
                    final Partition partition = partitions.get(pop1);

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

                if(size>1)
                    Arrays.sort(table, 0, size);

                add(-1); // terminal dummy index

                int offt=0;
                int clop=getClopIndex(0);

                for(int i=1; i<size; ++i) {
                    int iclop = getClopIndex(i);
                    if(iclop!=clop) {
                        C2Builder c2Builder = builders.get(clop);
                        if(c2Builder!=null) {
                            R0Table r0t = build(pop0, offt, i - offt);
                            c2Builder.put(r2, r0t);
                        }
                        offt = i;
                        clop = iclop;
                    }
                }
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
}
