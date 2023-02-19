package mills.index.builder;

import mills.bits.PopCount;
import mills.index.GroupIndex;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.index.tables.C2Table;
import mills.index.tables.R0Table;
import mills.position.Positions;
import mills.ring.EntryMap;
import mills.util.IndexTable;
import mills.util.PopMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.09.22
 * Time: 19:09
 */
public class IndexGroup implements GroupIndex {

    final PopCount pop;

    final PopMap<C2Table> group;

    final IndexTable it;

    IndexGroup(PopCount pop, GroupBuilder builder) {
        this.pop = pop;
        this.group = builder.build(Table::new);
        this.it = IndexTable.sum(group.values(), C2Table::range);
    }

    /**
     * This table holds a reference to the group to prevent early garbage collection.
     */
    class Table extends C2Table {

        Table(PopCount pop, PopCount clop, EntryMap<R0Table> tables) {
            super(pop, clop, tables.keySet(), tables.values());
        }

        Table(C2Builder builder) {
            this(pop, builder.clop, builder.tables());
        }
    }

    public PopMap<C2Table> group() {
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

        assert verify(i201);

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
