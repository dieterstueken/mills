package mills.index.builder;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.EntryMap;
import mills.util.IndexTable;
import mills.util.ListMap;
import mills.util.PopMap;

import java.util.function.Function;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.09.22
 * Time: 19:09
 */
public class IndexGroup implements PosIndex {

    final PopCount pop;

    final PopMap<C2Table> group;

    final IndexTable it;

    IndexGroup(PopCount pop, Function<IndexGroup, PopMap<C2Table>> builder) {
        this.pop = pop;
        this.group = builder.apply(this);
        this.it = IndexTable.sum(group.values(), C2Table::range);
    }

    C2Table newGroupIndex(PopCount clop, EntryMap<R0Table> r0Tables) {
        return new C2Table(pop, clop, r0Tables.keySet(), r0Tables.values()) {};
    }

    public ListMap<PopCount, C2Table> group() {
        return group;
    }

    public PosIndex getIndex(PopCount clop) {
        return clop == null ? this : group.get(clop);
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public int range() {
        return it.range();
    }

    public int posIndex(long i201) {

        if (!Positions.normalized(i201))
            i201 = normalize(i201);
        else
            assert i201 == normalize(i201);

        PopCount clop = Positions.clop(i201);
        int pos = group.keySet().indexOf(clop);
        int posIndex = group.getValue(pos).posIndex(i201);
        int baseIndex = it.baseIndex(pos);

        // if missing return lower bound by negative index
        if (posIndex < 0)
            posIndex -= baseIndex;
        else
            posIndex += baseIndex;

        return posIndex;
    }

    public long i201(int posIndex) {
        int pos = it.indexOf(posIndex);
        posIndex -= it.baseIndex(pos);
        C2Table c2t = group.getValue(pos);
        return c2t.i201(posIndex);
    }

    public IndexProcessor process(IndexProcessor processor, int start, int end) {

        for (int pos = it.indexOf(start); pos < group.size(); ++pos) {
            int baseIndex = it.baseIndex(pos);
            if (end < baseIndex)
                break;

            C2Table c2t = group.getValue(pos);
            int pstart = Math.max(0, start - baseIndex);
            c2t.process(processor, pstart, end - baseIndex);
        }

        return processor;
    }

    @Override
    public int n20() {
        int n20 = 0;
        for (int pos = 0; pos < group.size(); ++pos)
            n20 += group.getValue(pos).n20();
        return n20;
    }
}
