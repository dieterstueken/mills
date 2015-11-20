package mills.index4;

import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.util.IndexTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  20.11.2015 15:52
 * modified by: $Author$
 * modified on: $Date$
 */
public class ClopIndex implements PosIndex {

    final PopCount pop;

    final Map<PopCount, PosIndex> subsets;

    final List<PosIndex> clops;

    private final IndexTable index;

    @Override
    public PopCount pop() {
        return pop;
    }

    public ClopIndex(PopCount pop, Map<PopCount, PosIndex> subsets) {
        this.pop = pop;
        this.subsets = subsets;

        this.clops = new ArrayList<>();
        PopCount.CLOSED.stream().map(subsets::get).filter(Objects::nonNull).forEach(clops::add);
        this.index = IndexTable.sum(clops, PosIndex::range);
    }

    public PosIndex clopIndex(PopCount clop) {
        return clops.get(clop.index);
    }

    public int offset(PopCount clop) {
        return clop.index==0 ? 0 : index.get(clop.index-1);
    }

    @Override
    public int range() {
        return index.range();
    }

    @Override
    public int posIndex(long i201) {
        assert verify(i201) : Positions.position(i201);

        i201 = Positions.normalize(i201);
        PopCount clop = Positions.clop(i201);

        int posIndex = clopIndex(clop).posIndex(i201);

        return posIndex<0 ? posIndex - offset(clop) : posIndex + offset(clop);
    }

    @Override
    public long i201(int posIndex) {
        int i = index.lowerBound(posIndex);
        posIndex -= index.get(i);
        return subsets.get(i).i201(posIndex);
    }

    @Override
    public IndexProcessor process(IndexProcessor processor, int start, int end) {
        int i = index.upperBound(start);
        int len = end-start;

        start -= index.get(i);

        while(len>0) {
            PosIndex pi = clops.get(i);
            int offset = index.get(i);
            end = Math.min(len, pi.range());

            pi.process((posIndex, i201) -> processor.process(posIndex + offset, i201), start, end);

            len -= end;
            start = 0;
        }

        return processor;
    }
}
