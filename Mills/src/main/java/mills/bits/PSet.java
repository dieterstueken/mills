package mills.bits;

import mills.util.AbstractRandomArray;

import java.util.*;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 1/3/15
 * Time: 5:50 PM
 */
public class PSet extends AbstractSet<PGroup> {

    final int elements;

    private PSet(int elements) {
        this.elements = elements;
    }

    public static PSet empty() {
        return TABLE.get(0);
    }

    public PSet join(PGroup group) {
        int mask = 1<<group.ordinal();

        if((elements&mask)!=0)
            return this;

        return TABLE.get(elements | mask);
    }

    public PSet join(PSet other) {
        return TABLE.get(elements | other.elements);
    }

    public void forEach(Consumer<? super PGroup> action) {
        Objects.requireNonNull(action);

        for(int i=0,bits = elements; bits!=0; bits>>=3) {

            if(bits%8!=0) {
                if((bits&1)!=0)
                    action.accept(PGroup.get(i));
                ++i;
                if((bits&2)!=0)
                    action.accept(PGroup.get(i));
                ++i;
                if((bits&4)!=0)
                    action.accept(PGroup.get(i));
                ++i;

                bits >>= 8;
            }
        }
    }

    static class Itr implements Iterator<PGroup> {

        int elements;

        Itr(int elements) {
            this.elements = elements;
        }

        @Override
         public boolean hasNext() {
             return elements!=0;
         }

         @Override
         public PGroup next() {
             if (elements == 0)
                throw new NoSuchElementException();

             int element = elements & -elements;
             elements -= element;

             return PGroup.get(Integer.numberOfTrailingZeros(element));
         }
    }

    @Override
    public Iterator<PGroup> iterator() {
        return new Itr(elements);
    }

    @Override
    public int size() {
        return Integer.bitCount(elements);
    }

    @Override
    public boolean equals(Object o) {
        // singletons
        return (this == o);
    }

    @Override
    public int hashCode() {
        return elements;
    }

    public static final List<PSet> TABLE = AbstractRandomArray.generate(512, PSet::new);
}
