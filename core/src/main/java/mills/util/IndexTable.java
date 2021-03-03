package mills.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 12:47
 */
public abstract class IndexTable extends AbstractRandomList<Integer> {

    abstract public int getIndex(int i);

    abstract public int findIndex(int index);

    public int lowerBound(int index) {
        index = findIndex(index);
        return index<0 ? -(index+1) : index;
    }

    public int upperBound(int index) {
        index = findIndex(index);
        return index<0 ? -(index+1) : index+1;
    }

    public int baseIndex(int pos) {
        return pos==0 ? 0 : get(pos-1);
    }

    public int indexOf(int index) {
        return index==0 ? 0 : upperBound(index);
    }

    public int range() {
        return isEmpty() ? 0 : get(size()-1);
    }

    @Override
    public Integer get(int i) {
        return getIndex(i);
    }

    public static final IndexTable EMPTY = new IndexTable() {

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int range() {
            return 0;
        }

        public int getIndex(int i) {
            throw new IndexOutOfBoundsException("Index: "+i);
        }

        public int findIndex(int index) {
            return -1;
        }
    };

    public static IndexTable empty() {
        return EMPTY;
    }

    public static IndexTable singleton(final int index) {
        return new IndexTable() {

            @Override
            public int getIndex(int i) {
                if(i!=0)
                    throw new IndexOutOfBoundsException("Index: "+i);
                return index;
            }

            @Override
            public int findIndex(int i) {
                if(i<index)
                    return -1;
                if(i==index)
                    return 0;

                return -2;
            }

            @Override
            public int size() {
                return 1;
            }

            @Override
            public int range() {
                return index;
            }
        };
    }

    public static IndexTable of(int[] table) {

        if(table.length==0)
            return EMPTY;

        if(table.length==1)
            return singleton(table[0]);

        assert isOrdered(table);

        return new IndexTable() {

            @Override
            public int getIndex(int i) {
                return table[i];
            }

            @Override
            public int findIndex(int index) {
                return Arrays.binarySearch(table, index);
            }

            @Override
            public int size() {
                return table.length;
            }

            @Override
            public int range() {
                return table[table.length-1];
            }
        };
    }

    public static boolean isOrdered(int[] table) {
        if(table!=null && table.length>1) {
            int k = table[0];
            for (int i = 1; i < table.length; ++i) {
                int l = table[i];
                if(l<k)
                    return false;
                k = l;
            }
        }

        return  true;
    }

    public static final Indexer<List> SIZE = List::size;

    public static <E> IndexTable sum(List<? extends E> table, ToIntFunction<? super E> indexer) {

        if(table.isEmpty())
            return EMPTY;

        int sum = indexer.applyAsInt(table.get(0));
        int size = table.size();
        if(size==1)
            return singleton(sum);

        int[] index = new int[size];
        index[0] = sum;

        for(int i=1; i<size; ++i) {
            sum += indexer.applyAsInt(table.get(i));
            index[i] = sum;
        }

        return IndexTable.of(index);
    }

    public static <E> IndexTable sum0(final List<? extends E> table, final ToIntFunction<? super E> indexer) {

        int size = table.size();
        int index[] = new int[size];
        int sum = 0;

        for(int i=1; i<size; ++i) {
            sum += indexer.applyAsInt(table.get(i));
            index[i] = sum;
        }

        return IndexTable.of(index);
    }
}
