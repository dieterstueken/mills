package mills.score.opening;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Positions;

import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:36
 */
public class PlopSet extends Plop {

    final PosIndex index;

    final BitSet set;

    public PlopSet(Plop plop, PosIndex index) {
        super(plop);
        this.index = index;
        this.set = new BitSet(index.range());
    }

    public PopCount pop() {
        return index.pop();
    }

    public PopCount clop() {
        return index.clop();
    }

    public Clops clops() {
        return Clops.of(index);
    }

    public PopCount taken() {
        return plop.sub(index.pop());
    }

    public void set(int posIndex) {
        this.set.set(posIndex);
    }

    public boolean get(int posIndex) {
        return this.set.get(posIndex);
    }

    public void process(IndexProcessor processor) {
        int start = 0;

        while(true) {
            start = set.nextSetBit(start);
            if(start<0)
                break;

            int end = set.nextClearBit(start+1);
            if(end<0)
                end = this.index.range();

            this.index.process(processor, start, end);
            start = end+1;
        }
    }

    public void setPos(long i201) {
        assert pop().equals(Positions.pop(i201));
        int posIndex = index.posIndex(i201);
        set(posIndex);
    }
}
