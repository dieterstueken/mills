package mills.ring;

import mills.bits.BW;
import mills.bits.PopCount;
import mills.util.AbstractRandomList;

import java.util.ArrayList;
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
        final Comparator<RingEntry> poporder = Comparator.comparing(BW::pop, PopCount.ORDERING);
        final Comparator<RingEntry> sort = poporder.thenComparing(natural);

        RingEntry minimized[] = root.toArray();
        Arrays.sort(minimized, sort);
        final List<RingEntry> ordered = Arrays.asList(minimized);

        int start = 0;
        PopCount pop = PopCount.TABLE.get(0);

        final List<EntryTable> table = new ArrayList<>(PopCount.SIZE);

        for(int i=0; i<minimized.length; ++i) {
            RingEntry e = minimized[i];
            PopCount p = e.pop();
            if(!p.equals(pop)) {

                assert p.index() == table.size();

                EntryTable et = EntryTable.of(ordered.subList(start, i));
                table.add(et);

                pop = p;
                start = i;
            }
        }

        assert pop.index() == PopCount.SIZE-1;

        EntryTable et = EntryTable.of(ordered.subList(start, ordered.size()));
        table.add(et);

        assert table.size() == PopCount.SIZE;

        return new PopTable() {

            @Override
            public int size() {
                return PopCount.SIZE;
            }

            @Override
            public EntryTable get(int index) {
                return table.get(index);
            }
        };
    }
}
