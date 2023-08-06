package mills.score.attic.opening;

import mills.bits.Clops;
import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.PosIndex;
import mills.position.Positions;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 09.11.19
 * Time: 17:36
 */
public class PlopSet {

    final Plop plop;

    final PosIndex index;

    final BitSet set;

    public PlopSet(Plop plop, PosIndex index) {
        this.plop=plop;
        this.index = index;
        this.set = new BitSet(index.range());
    }

    public Player player() {
        return plop.player();
    }

    public PopCount pop() {
        return index.pop();
    }

    public PopCount clop() {
        return index.clop();
    }

    public Clops clops() {
        return index.clops();
    }

    public PopCount taken() {
        return plop.pop.sub(index.pop());
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

    @Override
    public String toString() {
        return clops() + "/" + plop.toString();
    }

    static final int BLOCK = 1024;

    public void processParallel(IndexProcessor processor) {
        int blocks = (index.range() + BLOCK - 1)/BLOCK;
        IntStream.range(0, blocks).parallel().forEach(start -> index.process(processor, start, start+BLOCK));
    }
}
