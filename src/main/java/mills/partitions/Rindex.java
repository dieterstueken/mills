package mills.partitions;

import com.google.common.collect.ImmutableList;
import mills.bits.BW;
import mills.bits.PopCount;
import mills.index.IndexList;
import mills.index.IndexedMap;
import mills.index.R0Table;
import mills.index.R2Table;
import mills.index.partitions.LePopTable;
import mills.main.C0Builder;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static Rindex build(PopCount pop) {
        return new Builder(pop).build();
    }

    static class Builder {

        final LePopTable lePop = LePopTable.open();
        final Partitions partitions = Partitions.get();

        final PopCount pop;

        final boolean concurrent = true;

        Builder(PopCount pop) {
            this.pop = pop;
        }

        Rindex build() {

            R2Table tables[] = new R2Table[25];

            List<RecursiveAction> tasks = new ArrayList<>(25);

            PopCount.TABLE.subList(0, 25).forEach(clop -> {

                if (concurrent && clop.le(pop.mclop())) {
                    tasks.add(new RecursiveAction() {
                        @Override
                        protected void compute() {
                            tables[clop.index] = r2Table(clop);
                        }

                        @Override
                        public String toString() {
                            return String.format("r2build:%d", clop.index);
                        }
                    });
                } else
                    tables[clop.index] = r2Table(clop);
            });

            ForkJoinTask.invokeAll(tasks);

            return new Rindex(pop, ImmutableList.copyOf(tables));
        }

        R2Table r2Table(PopCount clop) {

            if (!clop.le(pop.mclop()))
                return R2Table.of(pop, EntryTable.EMPTY, Collections.emptyList());

            EntryTable t2 = lePop.get(pop);

            if (t2.isEmpty())
                return R2Table.of(pop, EntryTable.EMPTY, Collections.emptyList());

            List<RingEntry> l2 = new ArrayList<>(t2.size());
            List<R0Table> l0 = new ArrayList<>(t2.size());

            for (RingEntry r2 : t2) {
                if (!r2.clop().le(clop)) // to many closed mills
                    continue;

                R0Table t0 = r0Table(clop, r2);
                if (t0.isEmpty())
                    continue;

                l2.add(r2);
                l0.add(t0);
            }

            return R2Table.of(pop, EntryTable.of(l2), ImmutableList.copyOf(l0));
        }

        private R0Table r0Table(PopCount clop, RingEntry r2) {
            PopCount pop2 = pop.sub(r2.pop);
            PopCount clop2 = clop.sub(r2.clop());
            assert clop2 != null;

            EntryTable t0 = lePop.get(pop2);

            if (t0.isEmpty())
                return R0Table.EMPTY;

            int count = t0.upperBound(r2);

            List<RingEntry> l0 = new ArrayList<>(count);
            short l1[] = new short[count];

            for (RingEntry r0 : t0) {
                // i2<=t0
                if (r0.index > r2.index)
                    break;

                // remaining closed mills
                PopCount clop1 = clop2.sub(r0.clop());
                if (clop1 == null)
                    continue;

                // won't be null since lepop
                PopCount pop1 = pop2.sub(r0.pop);
                int msk = r2.mlt20s(r0);
                int radials = Radials.index(r2, r0);

                int key = partitions.getKey(pop1, msk, clop1, radials);
                if (key == 0)
                    continue;

                assert verify(clop, r2, r0, key);

                int i = l0.size();
                l0.add(r0);
                l1[i] = (short) key;
            }

            if (l0.isEmpty())
                return R0Table.EMPTY;

            R0Table result = R0Table.of(EntryTable.of(l0), partitions.table(l1, l0.size()));

            return result;
        }

        boolean verify(PopCount clop, RingEntry r2, RingEntry r0, int key) {
            EntryTable t0 = partitions.getTable(key);

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

    public static void main(String... args) {
       serial();
    }

    static void serial() {
        for (int nw = 0; nw < 10; ++nw)
        for (int nb = 0; nb < 10; ++nb) {
            PopCount pop = PopCount.of(nb, nw);
            Rindex rindex = Rindex.build(pop);
            System.out.format("%s%,15d %,d\n", rindex.pop, rindex.range(), rindex.count());
        }
    }


    static void parallel() {

        List<RecursiveTask<Rindex>> tasks = new ArrayList<>(100);

        AtomicInteger working = new AtomicInteger(0);
        AtomicInteger waiting = new AtomicInteger(0);

        for (int nw = 0; nw < 10; ++nw)
            for (int nb = 0; nb < 10; ++nb) {
                PopCount pop = PopCount.of(nb, nw);

                RecursiveTask<Rindex> task = new RecursiveTask<Rindex>() {
                    @Override
                    protected Rindex compute() {
                        working.incrementAndGet();
                        Rindex rindex = Rindex.build(pop);
                        working.decrementAndGet();
                        waiting.incrementAndGet();
                        return rindex;
                    }
                };

                task.fork();
                tasks.add(task);
            }

        for (int i = 0; i < tasks.size(); i++) {
            RecursiveTask<Rindex> task = tasks.get(i);
            Rindex rindex = task.join();
            System.out.format("%s%10d working: %d waiting: %d\n", rindex.pop, rindex.range(), working.get(), waiting.get());
            tasks.set(i, null);
            waiting.decrementAndGet();
        }
    }

    static void r30() {
        IndexList indexes = IndexList.create();

        PopCount pop = PopCount.of(3, 0);
        Rindex rIndex = Rindex.build(pop);
        final R2Table pIndex = new C0Builder().buildR2(pop);

        System.out.format("%s%10d%10d\n", pop, rIndex.it.range(), pIndex.range());

    }
}
