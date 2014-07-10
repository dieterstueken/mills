package mills.index;

import com.google.common.collect.ImmutableList;
import mills.bits.PopCount;
import mills.position.Positions;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 15:01
 */
public class R2Index implements PosIndex {

    final PopCount pop;

    // partial table
    final List<R2Entry> entries;

    public R2Index(final PopCount pop, final List<R2Entry> entries) {
        this.pop = pop;
        this.entries = ImmutableList.copyOf(entries);
    }

    public final PopCount pop() {
        return pop;
    }

    public List<R2Entry> values() {
        return entries;
    }

    public int range() {
        if(entries.isEmpty())
            return 0;

        int tail = entries.size()-1;
        return entries.get(tail).size();
    }

    boolean verify(long i201) {
        PopCount p = Positions.pop(i201);
        return p == pop;
    }

    public int posIndex(long i201) {
        assert verify(i201) : Positions.position(i201);

        final long n201 = Positions.normalize(i201);

        final short i2 = Positions.i2(n201);
        int pos = R2Entry.R2.binarySearchKey(entries, i2);

        if (pos < 0) {
            // if missing return lower bound by negative index
            return -entries.get(-2 - pos).index;
        }

        return entries.get(pos).posIndex(i201);
    }

    public long i201(int posIndex) {

        final int pos = R2Entry.INDEX.lowerBound(entries, posIndex);

        // may throw IndexOutOfBoundsException
        final R2Entry entry = entries.get(pos);

        return entry.i201(posIndex);
    }

    public IndexProcessor process(IndexProcessor receiver) {
        return process(receiver, 0, Integer.MAX_VALUE);
    }

    public IndexProcessor process(IndexProcessor processor, int start, int end) {
        final int i0 = start>0 ? R2Entry.INDEX.lowerBound(entries, start) : 0;

        for(int i=i0; i<entries.size(); ++i) {
            final R2Entry entry = entries.get(i);
            if(!entry.process(processor, start, end))
                break;
        }

        return processor;
    }
}
