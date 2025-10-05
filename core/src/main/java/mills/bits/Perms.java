package mills.bits;

import mills.util.Indexed;
import mills.util.listset.AbstractIndexedSet;
import mills.util.listset.AbstractListSet;
import mills.util.listset.DirectListSet;
import mills.util.listset.ListSet;

import java.util.Iterator;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Class Perms provides predefined bitmasks of permutations.
 */
public class Perms extends AbstractIndexedSet<Perm> implements Indexed, Iterator<Perms> {

    protected final int perms;

    protected final int size;

    // perms[8] of 4 bits (bit 3 always set to mark validity)
    public final int bitseq;

    public static final int MSK = 0x7;
    public static final int FLAG = MSK+1;

    private Perms(int perms) {
        if((perms&0xff) != perms)
            throw new IllegalArgumentException();

        this.perms = perms;
        this.size = Integer.bitCount(perms&0xff);
        this.bitseq = bitseq(perms);
    }

    @Override
    public int getIndex() {
        return perms;
    }

    @Override
    public int size() {
        return size;
    }

    public int perm(int index) {
        assert inRange(index);
        int perm = (bitseq>>(4*index));
        assert (perm&FLAG)!=0;
        return perm&MSK;
    }

    @Override
    public Perm get(int index) {
        return Perm.get(perm(index));
    }

    @Override
    public boolean hasNext() {
        return true;
    }

    @Override
    public Perms next() {
        int next = getIndex();
        next ^= 1<<(bitseq&MSK);
        return of(next);
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Perm && this.contains((Perm)o);
    }

    public boolean contains(Perm p) {
        return (perms & p.msk()) != 0;
    }

    @Override
    public Perms subSet(final int offset, final int length) {
        assert inRange(size+offset);

        int mask = 0;

        for(int i=0; i<length; ++i) {
            int perm = perm(offset + i);
            mask |= 1<<perm;
        }

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

    /**
     * Generate a compact representation of indexes for size<8.
     *
     * @param perms 8 bit permutation mask.
     * @return a compact representation of indexes.
     */
    private static int bitseq(int perms) {
        // shift out indexes back to forth
        int bitseq = 0;
        for(int i=7; i>=0; --i) {
            int m = 1<<i;
            if((perms & m)!=0) {
                bitseq <<= 4;
                bitseq += i | FLAG;
            }
        }

        return bitseq;
    }

    /**
     * A specialized version of a compact range of bits.
     */
    static class Range extends Perms {

        private final int offset;

        private Range(int size, int offset) {
            super(((1 << size) - 1)<<offset);
            assert this.size() == size;
            this.offset = offset;
        }

        @Override
        public int perm(int index) {
            assert inRange(index);
            return index + offset;
        }
    }

    /**
     * A specialized version of all bits set < size
     */
    static class Direct extends Range implements DirectListSet<Perm> {

        private static Direct create(int size) {
            return size==0 ? new Empty() : new Direct(size);
        }

        static final List<Direct> VALUES = AbstractListSet.generate(9, Direct::create);

        private Direct(int size) {
            super(size, 0);
        }

        @Override
        public int perm(int index) {
            checkIndex(index);
            // direct mapping
            return index;
        }

        public Direct headList(int size) {
            checkRange(0, size);
            return VALUES.get(size);
        }
    }

    static class Empty extends Direct {

        // moved to here to prevent cyclic static dependencies
        static final ListSet<Perms> VALUES = createValues();

        Empty() {
            super(0);
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int getIndex() {
            return 0;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Perms next() {
            return null;
        }

        @Override
        public boolean contains(final Object o) {
            return false;
        }

        @Override
        public boolean contains(final Perm p) {
            return false;
        }

        @Override
        public Empty subSet(final int offset, final int size) {
            assert inRange(size+offset);
            return this;
        }
    }

    private static ListSet<Perms> createValues() {

        // setup values
        Perms[] values = new Perms[256];

        // setup direct perms and derived ranges.
        for (Direct direct : Direct.VALUES) {
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

        return DirectListSet.of(values);
    }

    static ListSet<Perms> values() {
        return Empty.VALUES;
    }

    public static Perms of(int perms) {
        return Empty.VALUES.get(perms&0xff);
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
