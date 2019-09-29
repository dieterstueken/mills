package mills.ring;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.09.2010
 * Time: 13:37:50
 */

import mills.bits.Pattern;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.IntFunction;
import java.util.stream.IntStream;

import static mills.ring.RingEntry.MAX_INDEX;

/**
 * Class RingTable is the complete EntryTable of 6561 RingEntries.
 */
class RingTable extends EntryTable {

    private final RingEntry[] entries = new RingEntry[MAX_INDEX];

    RingTable() {

        ForkJoinTask<IntFunction<Pattern>> patterns = new RecursiveTask<IntFunction<Pattern>>() {
            @Override
            protected IntFunction<Pattern> compute() {
                List<Pattern> patterns = Pattern.PATTERNS;
                return patterns::get;
            }
        }.fork();

        ForkJoinTask<?> fill = ForkJoinTask.adapt(()->
            Arrays.parallelSetAll(entries, index -> new RingEntry((short) index, patterns.join())))
                .fork();

        patterns.join();
        fill.join();
    }

    @Override
    public int size() {
        return MAX_INDEX;
    }

    @Override
    public RingEntry get(int index) {
        return entries[index];
    }

    boolean inRange(int index) {return index>=0 && index<MAX_INDEX;}

    // for the full table there is no need to search any entry.
    public int findIndex(int index) {
        return inRange(index) ? index : -1;
    }

    public short ringIndex(int index) {

        assert inRange(index) : "invalid RingTable index: " + index;

        return (short) index;
    }
}
