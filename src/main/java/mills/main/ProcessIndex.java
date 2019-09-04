package mills.main;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Positions;

import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 25.09.12
 * Time: 18:19
 */
public class ProcessIndex implements IndexProcessor {

    final PosIndex index;

    final ConcurrentNavigableMap<PopCount, LongAdder> stat = new ConcurrentSkipListMap<>();

    static final Function<PopCount, LongAdder> ADDER = popCount -> new LongAdder();

    ProcessIndex(PosIndex index) {
        this.index = index;
    }

    @Override
    public void process(int posIndex, long i201) {
        PopCount clop = Positions.clop(i201);
        stat.computeIfAbsent(clop, ADDER).increment();
    }

    public ProcessIndex run() {
        index.process(this);
        return this;
    }

    public void show() {

        int count = 0;

        PopCount max = index.pop().remains();

        for (Map.Entry<PopCount, LongAdder> e : stat.entrySet()) {
            if(e.getKey().le(max))
                count += e.getValue().sum();
        }

        System.out.format("pop: %s  %10d %10d\n", index.pop(), index.range(), count);

        for(int nb=0; nb<10; nb++) {
            PopCount p0 = PopCount.of(nb, 0);

            NavigableMap<PopCount, LongAdder> row = stat.tailMap(p0);
            if(row.isEmpty())
                break;

            System.out.format(" %d ", nb);

            for(int nw=0; nw<10; nw++) {
                PopCount pop = PopCount.of(nb, nw);
                LongAdder a = row.get(pop);
                if(a==null)
                    System.out.print("          ");
                else
                    System.out.format("%10d", a.sum());
            }

            System.out.println();
        }

        System.out.println();
    }

    public static void main(String ... args) throws InterruptedException {

        IndexProvider indexes = IndexProvider.load();

        for(PosIndex index:indexes) {
            new ProcessIndex(index).run().show();
        }

        System.out.println("ready");
    }
}
