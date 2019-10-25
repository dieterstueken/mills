package mills.bits;

import java.util.*;
import java.util.function.Consumer;

import static java.util.Spliterator.*;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  16.09.2019 11:53
 * modified by: $
 * modified on: $
 */
abstract public class Sectors extends AbstractSet<Sector> {

    final int pattern;

    protected Sectors(int pattern) {
        this.pattern = 0xff & pattern;
    }

    @Override
    public int size() {
        return Integer.bitCount(pattern);
    }

    @Override
    public boolean isEmpty() {
        return pattern==0;
    }

    /**
     * @param sector to test.
     * @return true if given sector is set.
     */
    public boolean test(Sector sector) {
        return (pattern & sector.mask()) != 0;
    }

    /**
     *  @return the first sector of this sector set or null if empty.
     */
    public Sector peek() {
        if(pattern==0)
            return null;

        int m = Integer.lowestOneBit(pattern);
        int k = Integer.numberOfTrailingZeros(m);
        return Sector.SECTORS.get(k);
    }

    public void forEach(Consumer<? super Sector> action) {
        int remaining = pattern;
        while(remaining!=0) {
            int m = Integer.lowestOneBit(remaining);
            if(m==0)
                break;
            remaining ^= m;
            int k = Integer.numberOfTrailingZeros(m);
            action.accept(Sector.SECTORS.get(k));
        }
    }

    @Override
    public Iterator<Sector> iterator() {
        return new SectorIterator(pattern);
    }

    private static class SectorIterator implements Iterator<Sector> {

        int remaining;

        private SectorIterator(int remaining) {
            this.remaining = remaining;
        }

        @Override
        public boolean hasNext() {
            return remaining!=0;
        }

        @Override
        public Sector next() {
            int m = Integer.lowestOneBit(remaining);
            if(m==0)
                throw new NoSuchElementException();

            remaining ^= m;
            int k = Integer.numberOfTrailingZeros(m);
            return Sector.SECTORS.get(k);
        }
    }

    @Override
    public Spliterator<Sector> spliterator() {
        return Spliterators.spliterator(this, DISTINCT | NONNULL | IMMUTABLE | SORTED);
    }
}
