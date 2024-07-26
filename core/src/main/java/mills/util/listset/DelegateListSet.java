package mills.util.listset;

import java.util.Comparator;
import java.util.List;
import java.util.Spliterator;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 21.08.22
 * Time: 15:25
 */
class DelegateListSet<T> extends AbstractListSet<T> {

    protected final List<T> values;

    private final Comparator<? super T> comparator;

    public DelegateListSet(List<T> values, Comparator<? super T> comparator) {
        this.values = values;
        this.comparator = comparator;
    }

    @Override
    public Comparator<? super T> comparator() {
        return comparator;
    }

    @Override
    public T get(int index) {
        return values.get(index);
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public ListSet<T> subSet(int offset, int size) {
        if(offset==0 && size==values.size())
            return this;

        List<T> subSet = values.subList(offset, offset+size);
        return new DelegateListSet<>(subSet, comparator);
    }

    @Override
    public boolean add(T t) {
        int index = findIndex(t);

        // already contained
        if (index >= 0)
            return false;

        values.add(-index - 1, t);

        return true;
    }

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public boolean remove(Object o) {
        int index = findIndex((T)o);
        if(index<0)
            return false;

        values.remove(index);

        return true;
    }

    @Override
    public T remove(int index) {
        return values.remove(index);
    }

    protected AbstractListSet<T> verify() {
        assert isOrdered(this, comparator()) : "index mismatch";
        return this;
    }

    @Override
    public Spliterator<T> spliterator() {
        return values.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return values.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return values.parallelStream();
    }

    static <T> boolean isOrdered(List<T> values, Comparator<? super T> order) {

        if(values.size()<2)
            return true;

        T t0 = values.getFirst();
        for (int i = 1; i < values.size(); ++i) {
            T t1 = values.get(i);
            if(order.compare(t0, t1)>=0)
                return false;
            t0 = t1;
        }

        return  true;
    }

    static <T> ListSet<T> of(List<T> values, Comparator<? super T> comparator) {
        assert isOrdered(values, comparator);
        return new DelegateListSet<>(values, comparator);
    }
}
