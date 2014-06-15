package mills.util;

import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 07.07.12
 * Time: 12:47
 */
public abstract class IndexTable extends AbstractRandomList<Integer> {

    abstract public int getIndex(int i);

    abstract public int lowerBound(int index);

    abstract public int range();

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

        public int lowerBound(int index) {
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
            public int lowerBound(int i) {
                return i<index ? -1 : 0;
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

    public static IndexTable of(final int table[], int size) {
        return of(Arrays.copyOf(table, size));
    }

    public static IndexTable of(final int table[]) {

        if(table.length==0)
            return EMPTY;

        return new IndexTable() {

            @Override
            public int getIndex(int i) {
                return table[i];
            }

            @Override
            public int lowerBound(int index) {
                int offset = Arrays.binarySearch(table, index);
                return offset<0 ? -2-offset : offset;
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

    public static Indexer<List> SIZE = new Indexer<List>() {

        @Override
        public int index(List element) {
            return element.size();
        }
    };

    public static <E> IndexTable build(final List<? extends E> table, final Indexer<E> indexer) {

        if(table.isEmpty())
            return EMPTY;

        int index[] = new int[table.size()];
        int idx = 0;

        for(int i=0; i<table.size(); ++i) {
            index[i] = idx;
            idx += indexer.index(table.get(i));
        }

        return IndexTable.of(index);
    }
}
