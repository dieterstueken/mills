package mills.index1.main;

import mills.bits.BW;
import mills.bits.PopCount;
import mills.index1.I2Entry;
import mills.index1.IndexList;
import mills.index1.R2Index;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 7/19/15
 * Time: 11:29 AM
 */
public class ClopFilter extends RecursiveAction {

    final Set<EntryTable> fragments = new TreeSet<>(Entries.BY_SIZE);
    int count;

    final R2Index posIndex;
    final PopCount clop;

    public ClopFilter(R2Index posIndex, PopCount clop) {
        this.clop = clop;
        this.posIndex = posIndex;
    }

    public void compute() {

        for (I2Entry e2 : posIndex.values()) {
            for (Map.Entry<RingEntry, EntryTable> e0 : e2.values().entrySet()) {

                EntryTable fragment = e0.getValue().filter(filter(e2.r2(), e0.getKey()));
                if (!fragment.isEmpty()) {
                    ++count;

                    if (fragment.size() > 1)
                        fragments.add(fragment);
                }
            }
        }
    }

    private Predicate<RingEntry> filter(RingEntry e2, RingEntry e0) {
        return e1 -> BW.clop(e2, e0, e1) == clop;
    }

    private static List<ClopFilter> filter(R2Index posIndex) {
        int n20 = posIndex.n20();

        System.out.format("%s %,12d, %4d\n", posIndex.pop(), n20, posIndex.values().size());

        PopCount mclop = posIndex.pop().mclop();

        List<ClopFilter> filters = PopCount.CLOSED.stream()
                .filter(clop -> clop.le(mclop))
                .map(clop -> new ClopFilter(posIndex, clop))
                .collect(Collectors.toList());

        ForkJoinTask.invokeAll(filters);

        return filters;
    }

    public static void main(String... args) {
        IndexList indexes = IndexList.create();

        Set<PopCount> clops = new TreeSet<>();
        Set<PopCount> vclops = new TreeSet<>();

        Set<EntryTable> fragments = new TreeSet<>(Entries.BY_SIZE);

        PopCount.TABLE.stream().filter(p -> p.nb <= p.nw).forEach(p -> {
            R2Index posIndex = indexes.get(p);
            List<ClopFilter> filters = filter(posIndex);
            for (ClopFilter f : filters) {

                PopCount remain = p.sub(f.clop.swap());
                boolean valid = remain!=null && f.clop.le(remain.mclop());

                fragments.addAll(f.fragments);
                if (f.count > 0) {
                    clops.add(f.clop);
                    if(valid)
                        vclops.add(f.clop);
                }

                System.out.format("%s%s%,12d, %4d\n",
                        valid?" ":"#", f.clop, f.count, f.fragments.size());
            }
        });

        System.out.println("clops:");

        for (PopCount clop : clops) {
            System.out.format("%s%s\n", vclops.contains(clop) ? " " : "#", clop);
        }

        System.out.format("\n%d partitions\n", fragments.size());

        int len=0;
        int count=0;

        for (EntryTable t : fragments) {
            if(t.size()!=len) {
                if(count>0)
                    System.out.format("%3d %5d\n", len, count);
                count=0;
                len=t.size();
            }

            ++count;
        }

    }
}
