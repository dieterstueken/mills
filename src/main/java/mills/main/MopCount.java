package mills.main;

import mills.bits.BW;
import mills.bits.PopCount;
import mills.index.Partitions;
import mills.index.partitions.LePopTable;
import mills.index2.Mop;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.function.BinaryOperator;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 4/18/14
 * Time: 8:30 PM
 */
public class MopCount {

    final Set<EntryTable> entries =  new TreeSet<>();

    int count = 0;

    int size = 0;

    public MopCount() {}

    public String toString() {
        return String.format("%d/%,d/%,d", entries.size(), count, size);
    }

    public MopCount add(MopCount other) {

        if(other!=null) {
            entries.addAll(other.entries);
            count += other.count;
            size += other.size;
        }

        return this;
    }

    public MopCount add(EntryTable t) {
        if(t!=null && !t.isEmpty()) {
            ++count;
            size += t.size();
            entries.add(t);
        }

        return this;
    }

    public static final BinaryOperator<MopCount> ADD = (s1, s2) -> new MopCount().add(s1).add(s2);

    static class MopTable extends RecursiveTask<MopCount> implements Comparable<MopTable> {

        final Mop mop;
        final Partitions partitions = Partitions.open();
        final LePopTable lpt = partitions.lePopTable;

        MopTable(Mop mop) {
            this.mop = mop;
        }

        @Override
        protected MopCount compute() {
            MopCount mc = lpt.get(mop.count).parallelStream().map(this::count).reduce(ADD).orElse(null);

            if(mc!=null && mc.size>0)
                System.out.format("%s %s\n", mop, mc);

            return mc;
        }

        MopCount count(RingEntry e2) {
            PopCount pop2 = mop.count.sub(e2.pop);

            MopCount mc = new MopCount();

            for(RingEntry e0:lpt.get(pop2)) {

                PopCount pop0 = pop2.sub(e0.pop);
                if(pop0==null)
                    continue;

                short msk = e2.mlt20s(e0);

                EntryTable t0 = partitions.partitions.get(pop0).get(msk);

                EntryTable m0 = t0.filter(e1 -> BW.mcount(e2, e0, e1) == mop.closed);
                mc.add(m0);
            }

            return mc;
        }

        @Override
        public int compareTo(MopTable o) {
            return mop.compareTo(o.mop);
        }
    }

    public static List<Mop> mops(PopCount pop) {
        final List<Mop> mops = new ArrayList<>();
        for(int nb=0; nb<Mop.maxClosed(pop.nb); ++nb)
            for(int nw=0; nw<Mop.maxClosed(pop.nw); ++nw) {
                PopCount closed = PopCount.of(nb, nw);
                Mop mop = new Mop(pop, closed);
                mops.add(mop);
            }

        return mops;
    }

    public static void main(String ... args) {

        final DateFormat df = new SimpleDateFormat("HH:mm:ss");

        System.out.println( df.format(new Date()));

        final List<MopTable> tasks = new ArrayList<>();

        for (PopCount pop : PopCount.TABLE) {
            for (Mop mop : mops(pop)) {
                MopTable task = new MopTable(mop);
                tasks.add(task);
            }
        }

        RecursiveTask.invokeAll(tasks);

        Collections.sort(tasks);

        MopCount total = new MopCount();
        int count = 0;

        for (MopTable task : tasks) {
            MopCount mc = task.join();
            if(mc!=null && mc.size>0) {
                total.add(mc);
                ++count;
                System.out.format("%s %s\n", task.mop, mc);
            }
        }

        System.out.format("total: %d %s\n", count, total);

        System.out.println( df.format(new Date()));
    }
}
