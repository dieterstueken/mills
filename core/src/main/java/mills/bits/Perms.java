package mills.bits;

import mills.util.Indexed;
import mills.util.listset.*;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.09.19
 * Time: 18:03
 */
public class Perms extends AbstractIndexedSet<Perm> implements Indexed {

    // 3bit perms[7] | 3bit size | 8bit perms
    public final int bitseq;

    private Perms(int perm) {
        this.bitseq = bitseq(perm);
    }

    @Override
    public int getIndex() {
        return bitseq&0xFF;
    }

    @Override
    public int size() {
        return (bitseq>>8)&0x7;
    }

    public int perm(int index) {
        checkIndex(index);
        return bitseq>>(3*index + 11);
    }

    public Perm get(int index) {
        return Perm.get(perm(index));
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Perm && this.contains((Perm)o);
    }

    public boolean contains(Perm p) {
        return (bitseq & p.msk()) != 0;
    }

    @Override
    public ListSet<Perm> subSet(final int offset, final int size) {
        int mask = bitseq&0xFF;

        // reset all bits < offset
        mask &= ((1<<perm(offset))-1)^0xff;

        // reset all bits >= offset+size
        mask &= ((1<<perm(offset + size - 1)+1)-1);

        return of(mask);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%cx%02x:[", getClass().getSimpleName().charAt(0), getIndex()));
        String sep = "";
        for (Perm perm : this) {
            sb.append(sep);
            sb.append(perm.name());
            sep = "|";
        }
        sb.append("]");
        return sb.toString();
    }

    //////////////////////////////////////////

    /**
     * Generate a compact representation of indexes for size<8.
     *
     * @param perms 8 bit permutation mask.
     * @return a compact representation of indexes.
     */
    private static int bitseq(int perms) {
        // special case, no sequence needed.
        if(perms==0 || perms==0xff)
            return perms;

        // shift out indexes back to forth
        int bitseq = 0;
        int size = 0;
        for(int i=7; i>=0; --i) {
            int m = 1<<i;
            if((perms & m)!=0) {
                bitseq <<= 3;
                bitseq += i;
                ++size;
            }
        }

        assert size<8;

        // add 3 bit size information.
        bitseq <<= 3;
        bitseq += size;

        // add perms itself
        bitseq <<= 8;
        bitseq |= (perms&0xff);
        return bitseq;
    }

    static class Range extends Perms {

        private final int size;

        private final int offset;

        private Range(int size, int offset) {
            super(((1 << size) - 1)<<offset);
            this.size = size;
            this.offset = offset;
        }

        @Override
        public int size() {
            return size;
        }

        @Override
        public int perm(int index) {
            checkIndex(index);
            return index + offset;
        }
    }

    static class Direct extends Range implements DirectListSet<Perm> {

        private Direct(int size) {
            super(size, 0);
        }

        @Override
        public int perm(int index) {
            checkIndex(index);
            return index;
        }

        public Direct headList(int size) {
            return DIRECT.get(size);
        }
    }
    
    public static final int MSK = 7;

    static final IndexedListSet<Direct> DIRECT;

    static final ListSet<Perms> VALUES;

    static {
        // setup directs
        Direct[] directs = new Direct[9];
        Arrays.setAll(directs, Direct::new);
        DIRECT = ArraySet.of(directs);

        // setup values
        Perms[] values = new Perms[256];

        // setup direct perms.
        for (Direct direct : directs) {
            values[direct.getIndex()] = direct;
            int l = direct.size();
            for(int i=1; i<l; ++i) {
                Range range = new Range(l-i,i);
                values[range.getIndex()] = range;
            }
        }

        // setup remaining.
        for (int perm = 0; perm < 256; ++perm) {
            if (values[perm] == null) {
                values[perm] = new Perms(perm);
            }
        }

        VALUES = DirectListSet.of(values);
    }

    public static Perms of(int perms) {
        return VALUES.get(perms&0xff);
    }

    public static Perms of(Perm ... perms) {
        int perm = 0;

        for (Perm p : perms) {
            perm |= p.msk();
        }

        return of(perm);
    }

    public static List<Perms> listOf(int ... perms) {
        return IntStream.of(perms).mapToObj(Perms::of).toList();
    }
}
