package mills.index2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.10.19
 * Time: 13:36
 */
abstract public class Fragment {

    final EntryTable table;

    public Fragment(EntryTable table) {
        this.table = table;
    }

    abstract public EntryTable get(RingEntry rad);

    static final Fragment EMPTY = new Fragment(EntryTable.EMPTY) {
        @Override
        public EntryTable get(RingEntry rad) {
            return EntryTable.EMPTY;
        }

        @Override
        public String toString() {
            return String.format("F[]");
        }
    };

    public static Fragment singleton(PopCount clop, RingEntry entry) {

        return new Fragment(entry.singleton) {
            @Override
            public EntryTable get(RingEntry rad) {
                return entry.clop(rad).equals(clop) ? table : EntryTable.EMPTY;
            }

            @Override
            public String toString() {
                return String.format("F[1]");
            }
        };
    }

    public static Fragment of(PopCount clop, EntryTable table) {
        if(table.isEmpty())
            return EMPTY;

        if(table.size()==1)
            return singleton(clop, table.get(0));

        return new Fragment(table) {
            @Override
            public EntryTable get(RingEntry rad) {
                return table.filter(e -> e.clop(rad).equals(clop));
            }

            @Override
            public String toString() {
                return String.format("F[%d]", table.size());
            }
        };
    }
}
