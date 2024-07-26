package mills.bits;

import mills.util.Indexed;
import mills.util.listset.DirectListSet;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.09.19
 * Time: 18:03
 */
public class Perms extends AbstractSet<Perm> implements Indexed, Comparable<Perms> {

    public final int perms;

    public final int bitseq;

    private Perms(int perms) {
        this.perms = perms;
        this.bitseq = bitseq(perms);
    }

    @Override
    public int getIndex() {
        return perms;
    }

    @Override
    public int size() {
        return Integer.bitCount(perms);
    }

    @Override
    public boolean isEmpty() {
        return perms==0;
    }

    @Override
    public boolean contains(Object o) {
        return o instanceof Perm && this.contains((Perm)o);
    }

    public boolean contains(Perm p) {
        return (perms & p.msk()) != 0;
    }

    @Override
    public Spliterator<Perm> spliterator() {
        return Spliterators.spliterator(this, Spliterator.DISTINCT|Spliterator.IMMUTABLE);
    }

    public Perm first() {
        if(perms ==0)
            throw new NoSuchElementException("empty");

        int i = Integer.numberOfTrailingZeros(perms);
        return Perm.get(i);
    }

    public Perms next() {
        int l = Integer.lowestOneBit(perms);
        if(l==0)
            throw new NoSuchElementException("empty");

        return Perms.of(perms-l);
    }

    @Override
    public Iterator<Perm> iterator() {
        return new Itr(this);
    }

    @Override
    public int compareTo(Perms o) {
        return Indexed.super.compareTo(o);
    }

    static class Itr implements Iterator<Perm> {
        Perms current;

        public Itr(Perms current) {
            this.current = current;
        }

        @Override
        public boolean hasNext() {
            return !current.isEmpty();
        }

        @Override
        public Perm next() {
            Perm next = current.first();
            current = current.next();
            return next;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02x:[", perms));
        String sep = "";
        for (Perm perm : this) {
            sb.append(sep);
            sb.append(perm.name());
            sep = "|";
        }
        sb.append("]");
        return sb.toString();
    }

    // fill a long with byte indexes of set bits.
    // bit 0 is ignored

    static int bitseq(int perms) {
        int bits = 0;
        for(int i=7; i>0; --i) {
            int m = 1<<i;
            if((perms & m)!=0) {
                bits <<= 4;
                bits += i;
            }
        }

        return bits;
    }

    //////////////////////////////////////////

    public static final DirectListSet<Perms> VALUES = DirectListSet.of(new Perms[256], Perms::new);

    public static final Perms EMPTY = of(0);

    public static final Perms OTHER = of(0xfe);

    public static final int MSK = 7;

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
