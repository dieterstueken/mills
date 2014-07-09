package mills.ring;

import mills.bits.BW;
import mills.bits.PopCount;
import mills.util.AbstractRandomList;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/3/14
 * Time: 6:11 PM
 */
abstract public class PopTable extends AbstractRandomList<EntryTable> {

    static PopTable create(EntryTable root) {

        final Comparator<RingEntry> natural = Comparator.naturalOrder();
        final Comparator<RingEntry> poporder = Comparator.comparing(BW::pop, PopCount.INDEX_ORDER);
        final Comparator<RingEntry> sort = poporder.thenComparing(natural);

        RingEntry minimized[] = root.toArray();
        Arrays.sort(minimized, sort);
        final List<RingEntry> ordered = Arrays.asList(minimized);

        int start = 0;
        PopCount pop = PopCount.TABLE.get(0);

        final EntryTable tables[] = new EntryTable[PopCount.SIZE];
        Arrays.fill(tables, EntryTable.EMPTY);

        for(int i=0; i<minimized.length; ++i) {
            RingEntry e = minimized[i];
            PopCount p = e.pop();
            if(!p.equals(pop)) {

                EntryTable et = EntryTable.of(ordered.subList(start, i));
                tables[pop.index] = et;

                pop = p;
                start = i;
            }
        }

        EntryTable et = EntryTable.of(ordered.subList(start, ordered.size()));
        tables[pop.index] = et;

        return new PopTable() {

            @Override
            public int size() {
                return PopCount.SIZE;
            }

            @Override
            public EntryTable get(int index) {
                return tables[index];
            }
        };
    }
}
