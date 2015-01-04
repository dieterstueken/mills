package mills.partitions;

import mills.bits.*;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;
import mills.ring.RingTable;
import mills.util.AbstractRandomArray;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.RecursiveTask;
import java.util.function.Predicate;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 1/2/15
 * Time: 5:48 PM
 */
public class Builder extends RecursiveTask<Partitions> {

    EntryTables registry = new EntryTables();

    @Override
    protected Partitions compute() {

        ForkJoinTask<List<EntryTable>> lePops = new LePops();

        RecursiveTask<List<Partition>> partitions = new PartitionsBuilder();

        invokeAll(lePops, partitions);

        return new Partitions(lePops.join(), partitions.join());
    }


    static class LePops extends RecursiveTask<List<EntryTable>> {

        final int p88 = PopCount.of(8,8).index;

        final List<ForkJoinTask<EntryTable>> tasks = new ArrayList<>(p88);

        private class Task extends RecursiveTask<EntryTable> {

            final PopCount pop;

            Task(PopCount pop) {
                this.pop = pop;
            }

            @Override
            protected EntryTable compute() {

                if(pop.max()==0)
                    return RingEntry.of(0).singleton;

                // should not happen ...
                // if(pop.min()>=8)
                //    return RingEntry.TABLE;

                // filter some table containing all required entries;
                // increment either the bigger one if < 8 or else the other one.
                Player up = pop.nw<pop.nb && pop.nw<8 ? Player.White : Player.Black;
                PopCount pup = pop.add(up.pop);
                EntryTable table = getTable(pup.index);
                table = table.le(pop);

                assert table.equals(RingEntry.TABLE.filter(pop.le));

                return table;
            }
        }

        EntryTable getTable(int index) {
            if(index<tasks.size())
                return tasks.get(index).join();
            else
                return RingEntry.TABLE;
        }

        @Override
        protected List<EntryTable> compute() {

            for(int pop=0; pop<p88; ++pop)
                tasks.add(new Task(PopCount.get(pop)));

            invokeAll(tasks);

            return AbstractRandomList.generate(PopCount.SIZE, this::getTable);
        }
    }

    class PartitionsBuilder extends RecursiveTask<List<Partition>> {

        final Partition partitions[] = new Partition[PopCount.SIZE];

        @Override
        protected List<Partition> compute() {

            // preset to all empty
            Arrays.fill(partitions, Partition.EMPTY);

            final List<ForkJoinTask<?>> tasks = new ArrayList<>(55);

            for (PopCount pop : PopCount.TABLE) {
                EntryTable root = RingEntry.MINIMIZED.eq(pop);
                if(root.isEmpty())
                    continue;

                tasks.add(new RecursiveAction() {
                    @Override
                    protected void compute() {
                        Partition partition = build(root);
                        partitions[pop.index] = partition;
                    }
                });
            }

            invokeAll(tasks);

            return Arrays.asList(partitions);
        }
    }

    final List<Predicate<RingEntry>> pgFilters = AbstractRandomArray.generate(128, msk -> e -> e.stable(2 * msk));

    Partition build(EntryTable root) {

        if(root.isEmpty())
            return Partition.EMPTY;

        PSet pset = PSet.empty();
        for (RingEntry e : root)
            pset = pset.join(e.grp);

        List<ForkJoinTask<Partition.Group>> tasks = new ArrayList<>();
        List<ForkJoinTask<Partition.Group>> groups = new ArrayList<>(128);
        for(int msk=0; msk<128; ++msk)
            groups.add(null);

        for(int msk=0; msk<128; ++msk) {

            ForkJoinTask<Partition.Group> task = groups.get(msk);

            if (task == null) {
                int mx = 127;
                for (PGroup pg : pset) {
                    // add all unset bits of permitted
                    if (!pg.collides(msk))
                        mx &= 127 ^ pg.msk;
                }

                task = groups.get(mx);
                if(task==null) {

                    Predicate<RingEntry> filter = pgFilters.get(msk);

                    task = new RecursiveTask<Partition.Group>() {

                        @Override
                        protected Partition.Group compute() {
                            return group(root.filter(filter));
                        }
                    };

                    groups.set(mx, task);
                    tasks.add(task);
                }

                groups.set(mx, task);
            }

            groups.set(msk, task);
        }

        invokeAll(tasks);

        List<Partition.Group> list = AbstractRandomList.map(groups, ForkJoinTask::join);
        List<Partition.Group> gset = AbstractRandomList.map(tasks, ForkJoinTask::join);

        return new Partition(list, gset);
    }

    final List<Predicate<RingEntry>> clopFilters = AbstractRandomList.generate(25 * 81, i -> {
        RingEntry rad = RingEntry.radix(i / 25);
        PopCount clop = PopCount.TABLE.get(i % 25);
        return e -> clop.nb == e.b.mcount + e.b.and(rad.b).count
                 && clop.nw == e.w.mcount + e.w.and(rad.w).count;
    });

    Predicate<RingEntry> clopFilter(RingEntry rad, PopCount clop) {
        return clopFilters.get(25 * rad.radix() + clop.index);
    }

    static short[] empty() {
        short empty[] = new short[25];
        Arrays.fill(empty, (short) -1);
        return empty;
    }

    Partition.Group group(EntryTable root) {

        if (root.isEmpty())
            return Partition.EMPTY.group(0);


        // accumulate black and white radials
        Pattern b = Pattern.NONE;
        Pattern w = Pattern.NONE;

        for (RingEntry e : root) {
            b = b.or(e.b);
            w = w.or(e.w);
        }

        b = b.and(Pattern.RADIALS);
        w = w.and(Pattern.RADIALS);

        final short map[][] = new short[81][];
        int nc = 0;

        for (int ir = 0; ir < 81; ++ir) {
            RingEntry rad = RingEntry.radix(ir);

            // reset all radials which are not set by any root entry
            RingEntry red = RingEntry.of(rad.b.and(b), rad.w.and(w));

            if (red != rad) {
                // copy entry
                map[ir] = map[red.radix()];
            } else {
                short it[] = null;
                for (PopCount clop : PopCount.CLOSED) {
                    EntryTable t = root.filter(clopFilter(rad, clop));
                    if (!t.isEmpty()) {
                        if (it == null)
                            it = empty();
                        it[clop.index] = registry.index(t);
                    }
                }

                if (it != null)
                    ++nc;

                map[ir] = it;
            }
        }

        if (nc == 0)
            return Partition.Group.EMPTY;

        int count = nc;

        return new Partition.Group() {

            @Override
            public EntryTable get(int rad, int clop) {
                short ct[] = map[rad];
                return ct == null ? RingTable.EMPTY : registry.get(ct[clop]);
            }

            @Override
            public EntryTable root() {
                return root;
            }

            @Override
            public int count() {
                return count;
            }
        };
    }

    public static void main(String ... args) {

        long start = System.currentTimeMillis();

        Partitions partitions = new Builder().invoke();

        long stop = System.currentTimeMillis();

        for (PopCount pop : PopCount.TABLE) {
            Partition pt = partitions.partition(pop);
            List<? extends Partition.Group> gset = pt.gset();
            if(gset.isEmpty())
                continue;

            System.out.format("%s root: %d gset: %d\n", pop, pt.root().size(), gset.size());

            for (int i = 0; i < gset.size(); i++) {
                Partition.Group group = gset.get(i);
                System.out.format("  %2d: %d\n", i, group.count());
            }
        }

        System.out.format("total: %d ms  %s\n", stop-start, ForkJoinPool.commonPool().toString());
    }
}
