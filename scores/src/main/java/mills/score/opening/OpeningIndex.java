package mills.score.opening;

import mills.bits.PopCount;
import mills.index.PosIndex;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  05.03.2021 08:53
 * modified by: $
 * modified on: $
 */
abstract public class OpeningIndex extends OpeningLayer {

    final PosIndex index;

    OpeningIndex(PosIndex index, int turn) {
        super(turn);
        this.index = index;
        assert placed(turn).sub(index.clop())!=null;
    }

    @Override
    public PopCount clop() {
        return index.clop();
    }

    @Override
    public PopCount pop() {
        return index.pop();
    }

    public int range() {
        return index.range();
    }
}
