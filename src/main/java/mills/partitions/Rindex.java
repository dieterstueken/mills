package mills.partitions;

import com.google.common.collect.ImmutableList;
import mills.bits.BW;
import mills.bits.PopCount;
import mills.index.IndexList;
import mills.index.IndexedMap;
import mills.index.R0Table;
import mills.index.R2Table;
import mills.main.C0Builder;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  22.07.2014 14:26
 * modified by: $Author$
 * modified on: $Date$
 */
public class Rindex {

    final PopCount pop;

    final List<R2Table> table;  // clop[25]

    final IndexTable it;

    private Rindex(PopCount pop, List<R2Table> table) {
        this.pop = pop;
        this.table = table;
        this.it = IndexTable.sum(table, IndexedMap::range);
    }

    public final PopCount pop() {
        return pop;
    }

    private int range() {
        return it.range();
    }

    private int count() {
        int count = 0;
        for (R2Table r2Table : table) {
            for (R0Table r0Table : r2Table.values()) {
                count += r0Table.size();
            }
        }
        return count;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder implements Function<PopCount, Rindex> {

        final ForkJoinTask<Partitions> ptask = ForkJoinTask.adapt(Partitions::build).fork();

        final ConcurrentLinkedQueue<R0Builder> builders = new ConcurrentLinkedQueue<>();

        public Rindex apply(PopCount pop) {

            R2Table tables[] = new R2Table[25];

            List<RecursiveAction> tasks = new ArrayList<>(25);

            LePopTables.CLOP_TABLE.forEach(clop -> {

                if (clop.le(pop.mclop())) {
                    tasks.add(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            tables[clop.index] = r2Table(pop, clop);
                        }

                        @Override
                        public String toString() {
                            return String.format("r2build:%d", clop.index);
                        }
                    });
                } else
                    tables[clop.index] = r2Table(pop, clop);
            });

            ForkJoinTask.invokeAll(tasks);

            return new Rindex(pop, ImmutableList.copyOf(tables));
        }

        R2Table r2Table(PopCount pop, PopCount clop) {

            if (!clop.le(pop.mclop()))
                return R2Table.of(pop, EntryTable.EMPTY, Collections.emptyList());

            return new R2Builder(pop, clop).build();
        }

        class R2Builder {

            final Partitions partitions = ptask.join();

            final PopCount pop;

            final PopCount clop;

            R2Builder(PopCount pop, PopCount clop) {
                this.pop = pop;
                this.clop = clop;
            }

            R2Table build() {

                EntryTable t2 = partitions.lePop(pop, clop, pop.sum()-16);

                if (t2.isEmpty())
                    return R2Table.of(pop, EntryTable.EMPTY, Collections.emptyList());

                List<ForkJoinTask<R0Table>> tasks = new ArrayList<>(t2.size());

                for (RingEntry r2 : t2) {
                    tasks.add(task(r2));
                }

                ForkJoinTask.invokeAll(tasks);

                List<R0Table> t0 = new ArrayList<>(t2.size());
                short s2[] = new short[t2.size()];

                for (int i = 0; i < tasks.size(); i++) {
                    ForkJoinTask<R0Table> task = tasks.get(i);
                    R0Table r0 = task.join();
                    if (!r0.isEmpty()) {
                        s2[t0.size()] = t2.get(i).index;
                        t0.add(r0);
                    }
                }

                if (t0.size() != t2.size()) {
                    // supersede by shorter table
                    t2 = EntryTable.of(s2, 0, t0.size());
                }

                return R2Table.of(pop, t2, ImmutableList.copyOf(t0));
            }

            ForkJoinTask<R0Table> task(RingEntry r2) {

                return new RecursiveTask<R0Table>() {

                    @Override
                    protected R0Table compute() {
                        R0Builder builder = builders.poll();

                        if(builder==null)
                            builder = new R0Builder(partitions);

                        R0Table result = builder.build(pop, clop, r2);

                        builders.offer(builder);

                        return result;
                    }
                };

            }
        }

        /**
         * Class R0Builder generated R0Tables and is not thread safe
         */
        static class R0Builder {

            final Partitions partitions;

            final short l0[] = new short[RingEntry.MAX_INDEX];
            final short l1[] = new short[RingEntry.MAX_INDEX];

            R0Builder(Partitions partitions) {
                this.partitions = partitions;
            }

            R0Table build(PopCount pop, PopCount clop, RingEntry r2) {

                // remaining pop count for e1/e0
                PopCount pop2 = pop.sub(r2.pop);

                // remaining closed count for e0/e1
                PopCount clop2 = clop.sub(r2.clop());
                assert clop2 != null;

                EntryTable t0 = partitions.lePop(pop2, clop2, pop.sum()-pop2.sum()-8);

                if (t0.isEmpty())
                    return R0Table.EMPTY;

                int count = t0.upperBound(r2);

                // verify remaining elements are > r2.index
                assert count>=t0.size() || r2.index<t0.get(count).index;
                assert count==0 || r2.index>=t0.get(count-1).index;

                // truncate list;
                t0 = t0.subList(0, count);

                int size = 0;
                for (RingEntry r0 : t0) {

                    assert(r0.index <= r2.index);

                    // remaining closed mills
                    PopCount clop1 = clop2.sub(r0.clop());

                    // should not happen due to lePops
                    assert clop1!=null;

                    // won't be null since lepop
                    PopCount pop1 = pop2.sub(r0.pop);

                    assert pop1!=null;

                    // can't be fulfilled
                    if(pop1.sum()>8)
                        continue;

                    int msk = r2.mlt20s(r0);

                    if(partitions.getPartition(pop1, msk).isEmpty())
                        continue;

                    int radials = Radials.index(r2, r0);

                    int key = partitions.getKey(pop1, msk, clop1, radials);
                    if (key == 0)
                        continue;

                    assert verify(pop, clop, r2, r0, partitions.getTable(key));

                    // add entry
                    l0[size] = r0.index;
                    l1[size] = (short) key;
                    ++size;
                }

                if(size==0)
                    return R0Table.EMPTY;

                if(size!=t0.size()) {
                    // supersede by shorter table
                    t0 = EntryTable.of(l0, 0, size);
                }

                List<EntryTable> t1 = partitions.entryTables(l1, size);

                return  R0Table.of(t0, t1);
            }
        }

        static boolean verify(PopCount pop, PopCount clop, RingEntry r2, RingEntry r0, EntryTable t0) {
            //EntryTable t0 = partitions.getTable(key);

            for (RingEntry r1 : t0) {
                PopCount p = r2.pop().add(r0.pop).add(r1.pop);
                if (p != pop)
                    return false;

                p = BW.clop(r2, r0, r1);
                if (p != clop)
                    return false;
            }

            return true;
        }
    }

    public static void main(String... args) throws IOException {

        Rindex.Builder builder = Rindex.builder();

        //r88(builder);
        serial(builder);
    }

    static void stat(Rindex rindex ) {

        int stat[] = new int[16];
        long sizeOf = 0;

        for (R2Table r2t : rindex.table) {
            for (R0Table r0t : r2t.values()) {
                int n = Math.min(r0t.size(), 15);
                ++stat[n];

                sizeOf += n*(4+2) * 24;
            }
        }

        for (int i = 0; i < stat.length; i++) {
            int n = stat[i];
            System.out.format("%d %,d\n", i, n);
        }

        System.out.format("%,dk\n", sizeOf/1024);

    }

    static void serial(Function<PopCount, Rindex> builder) {

        ForkJoinTask<String> task = null;

        for (int nw = 0; nw < 10; ++nw)
        for (int nb = 0; nb < 10; ++nb) {
            PopCount pop = PopCount.of(nb, nw);

            final ForkJoinTask<String> prev = task;
            task = new RecursiveTask<String>() {

                @Override
                protected String compute() {

                    if(prev != null)
                        System.out.println(prev.join());

                    Rindex rindex = builder.apply(pop);
                    return String.format("%s%,15d %,d", rindex.pop, rindex.range(), rindex.count());
                }
            };

            task.fork();
        }

        if(task!=null)
            System.out.println(task.join());
    }

    static void r30(Function<PopCount, Rindex> builder) {
        IndexList indexes = IndexList.create();

        PopCount pop = PopCount.of(3, 0);
        Rindex rIndex = builder.apply(pop);
        final R2Table pIndex = new C0Builder().buildR2(pop);

        System.out.format("%s%10d%10d\n", pop, rIndex.it.range(), pIndex.range());
    }

    static void r88(Function<PopCount, Rindex> builder) throws IOException {
        PopCount pop = PopCount.of(8, 8);
        Rindex rIndex = builder.apply(pop);
        System.out.format("%s%10d%10d\n", pop, rIndex.it.range(), rIndex.range());

        System.in.read();
    }
}
