package mills.util;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 11.09.2010
 * Time: 10:30:09
 */
public class Index extends AbstractRandomList<Integer> {

    final int size;
    
    public Index(final int size) {
        this.size = size;
    }

    public static Index of(int i) {
        return new Index(i);
    }

    @Override
    public Integer get(int index) {
        if(index<0 || index>=size())
            throw new IndexOutOfBoundsException("Index: "+index);
        return index;
    }

    @Override
    public int size() {
        return size;
    }
}
