package mills.main;

import mills.bits.BW;
import mills.bits.PopCount;
import mills.index.Partitions;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.03.14
 * Time: 19:46
 */
public class MopIndex {

    MopIndex() {
        for(int ib=0; ib<5; ib++)
            for(int iw=0; iw<5; iw++)
                closed.add(PopCount.of(ib,iw));
    }

    final List<PopCount> closed = new ArrayList<>(25);

    final Partitions partitions = Partitions.open();

    final ConcurrentLinkedQueue<MopStat> queue = new ConcurrentLinkedQueue<>();

    MopStat stat(PopCount pop) {

        if(pop==null)
            return null;

        MopStat stat = new MopStat(pop);

        stat.fork();

        return stat;
    }

    class MopStat extends RecursiveAction implements Comparable<MopStat> {

        final PopCount pop;

        MopStat(PopCount pop) {
            this.pop = pop;
        }

        public String toString() {
            return String.format("MopStat: %s", pop);
        }

        final Set<EntryTable> entries = new TreeSet<>();

        int count = 0;

        void add(EntryTable entry) {
            if(!entry.isEmpty()) {
                entries.add(entry);
                ++count;
            }
        }

        void add(MopStat stat) {
            if(stat!=null) {
                //stat.join();
                entries.addAll(stat.entries);
                count += stat.count;
            }
        }

        @Override
        protected void compute() {

            //System.out.format("start %s\n", pop);

            partitions.lePopTable.get(pop);

            for (RingEntry e2:partitions.lePopTable.get(pop)) {

                PopCount pop2 = pop.sub(e2.pop);

                for(RingEntry e0:partitions.lePopTable.get(pop2)) {

                    PopCount pop0 = pop2.sub(e0.pop);
                    if(pop0==null)
                        continue;

                    short msk = e2.mlt20s(e0);

                    EntryTable t0 = partitions.partitions.get(pop0).get(msk);

                    for(PopCount clop:closed) {

                        EntryTable m0 = t0.filter(e1 -> BW.clop(e2, e0, e1) == clop);

                        add(m0);
                    }
                }
            }

            System.out.format("%s %12d %6d (%d)\n", pop, count, entries.size(), queue.size());

            while(!queue.isEmpty()) {
                MopStat stat = queue.poll();
                if(stat==null)
                    break;

                add(stat);
            }

            queue.add(this);
        }

        @Override
        public int compareTo(MopStat o) {
            int result = Integer.compare(pop.sum(), o.pop.sum());
            if(result==0)
                result = Integer.compare(pop.min(), o.pop.min());

            return -1*result;
        }
    }

    public void run() {

        List<MopStat> tasks = new ArrayList<>();

        for(PopCount pop:PopCount.TABLE) {

            if(pop.nb>pop.nw)
                continue;

            tasks.add(new MopStat(pop));
        }

        Collections.sort(tasks);

        ForkJoinTask.invokeAll(tasks);

        int size = queue.size();

        MopStat result = queue.poll();

        while(!queue.isEmpty()) {
            MopStat stat = queue.poll();
            if(stat==null)
                break;

            result.add(stat);
        }

        System.out.format("total: %12d %6d (%d)\n", result.count, result.entries.size(), size);

        List<AtomicInteger> stat = new ArrayList<>();

        for (EntryTable t : result.entries) {
            size = t.size();
            while(stat.size()<=size)
                stat.add(new AtomicInteger());

            stat.get(size).addAndGet(1);
        }

        for (int i=0; i<stat.size(); ++i) {
            System.out.format("%d %d\n", i, stat.get(i).get());
        }
    }

    //Partitions partitions = Partitions.create();

    public static void main(String ... args) {

        new MopIndex().run();
    }
}
