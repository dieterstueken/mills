package mills.index3;

import mills.bits.PopCount;
import mills.index3.partitions.ClopTable;
import mills.index3.partitions.MaskTable;
import mills.index3.partitions.Partitions;
import mills.index3.partitions.RadialTable;
import mills.ring.EntryTable;
import mills.ring.RingEntry;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/29/15
 * Time: 6:59 PM
 */
public class Builder {

    final Partitions partitions;

    public Builder(Partitions partitions) {
        this.partitions = partitions;
    }

    public List<R20Entry> entries(PopCount pop) {

        List<RecursiveTask<R20Entry>> tasks = new ArrayList<>();

        for (RingEntry e2 : partitions.get(pop).lePop) {

            // remaining pop
            PopCount pop2 = pop.sub(e2.pop);
            assert pop2!=null : "lePop underflow";

            for (RingEntry e0 : partitions.get(pop2).lePop) {
                if(e0.index>e2.index())
                    break;

                PopCount pop1 = pop2.sub(e0.pop);
                assert pop1!=null : "lePop underflow";
                MaskTable p1 = partitions.get(pop1);

                if(!p1.content().isEmpty())
                    tasks.add(entry(p1, e2, e0));
            }
        }

        ForkJoinTask.invokeAll(tasks);
        tasks.removeIf(task -> task.join()==null);

        return AbstractRandomList.virtual(tasks, ForkJoinTask::join);
    }

    public R2Tables build(PopCount pop) {
        MaskTable partition = partitions.get(pop);
        return build(partition, entries(pop));
    }

    static R2Tables build(MaskTable partition, List<R20Entry> entries) {

        List<List<R20Entry>> partitions = new ArrayList<>();

        int i0 = 0;
        RingEntry e2 = null;
        for(int i1=1; i1<entries.size(); ++i1) {
            R20Entry entry = entries.get(i1);
            if(!entry.e2.equals(e2)) {
                e2 = entry.e2;
                partitions.add(entries.subList(i0, i1));
                i0 = i1;
            }
        }

        EntryTable t2 = EntryTable.of(AbstractRandomList.virtual(partitions, list->list.get(0).e2));

        short i2[] = new short[t2.size()];
        short sum = 0;
        for(int i=0; i<t2.size(); ++i) {
            sum += partitions.get(i).size();
            i2[i] = sum;
        }

        short[] t0 = new short[entries.size()];
        short[] radmsk = new short[entries.size()];
        int[] index = new int[entries.size()];

        sum = 0;
        for(int i=0; i<entries.size(); ++i) {
            R20Entry entry = entries.get(i);
            t0[i] = entry.e0.index;
            radmsk[i] = (short)(256*entry.rad.index + entry.msk);
            index[i] = sum;
            sum += entry.rt.content().size();
        }

        List<ClopTable> r1 = new AbstractRandomList<ClopTable>() {

            @Override
            public int size() {
                return radmsk.length;
            }

            @Override
            public ClopTable get(int index) {
                index = radmsk[index];
                int msk = index&0xff;
                int rad = index/256;
                return partition.get(msk).get(rad);
            }
        };

        return new R2Tables(partition.pop(), t2, i2, t0, r1, index);

    }

    RecursiveTask<R20Entry> entry(MaskTable partition, RingEntry e2, RingEntry e0) {
        return new RecursiveTask<R20Entry>() {
            @Override
            protected R20Entry compute() {

                int msk = e2.mlt20s(e0);
                RadialTable rt = partition.get(msk);

                return rt.content().isEmpty() ? null : new R20Entry(e2, e0, msk, rt);
            }
        };
    }

    static class R20Entry {

        final RingEntry e2;
        final RingEntry e0;
        final RadialTable rt;

        final int msk;
        final RingEntry rad;

        R20Entry(RingEntry e2, RingEntry e0, int msk, RadialTable rt) {
            this.e2 = e2;
            this.e0 = e0;
            this.rt = rt;

            this.msk = msk;

            rad = e2.and(e0).radials();
        }

        @Override
        public String toString() {
            return String.format("[%d:%d] %02x %2d %d", e2.index, e0.index, msk, rad.radix(), rt.root.size());
        }
    }
}
