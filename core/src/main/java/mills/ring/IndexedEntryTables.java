package mills.ring;

import mills.util.AbstractRandomList;

import java.util.function.IntFunction;

/*
 * Class IndexedEntryTables represents a IndexedEntryTable list.
 */
public abstract class IndexedEntryTables extends AbstractRandomList<IndexedEntryTable> {

    @Override
    public IndexedEntryTable get(int index) {
        throw new IndexOutOfBoundsException("Index: " + index);
    }

    static final IndexedEntryTables EMPTY = new IndexedEntryTables() {

        @Override
        public int size() {
            return 0;
        }
    };

    public static IndexedEntryTables of() {
        return EMPTY;
    }

    public static IndexedEntryTables of(IndexedEntryTable table) {
        return of(table, 1);
    }

    public static IndexedEntryTables of(IndexedEntryTable table, int size) {
        return new IndexedEntryTables() {

            @Override
            public int size() {
                return 1;
            }

            @Override
            public IndexedEntryTable get(int index) {
                return index>=0 && index<size ? table : super.get(index);
            }
        };
    }

    static IndexedEntryTables of(short[] keys, IntFunction<IndexedEntryTable> getTable) {
        return new IndexedEntryTables() {

            @Override
            public int size() {
                return keys.length;
            }

            @Override
            public IndexedEntryTable get(int index) {
                return getTable.apply(keys[index]);
            }
        };
    }
}
