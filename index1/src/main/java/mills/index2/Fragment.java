package mills.index2;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.ring.RingEntry;

import java.util.List;
import java.util.Objects;

import static mills.index2.FragmentBuilder.RADS;

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

    public EntryTable get(int i) {
        return get(Entries.RADIALS.get(i));
    }

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

    static class VirtualFragment extends Fragment {

        final PopCount clop;

        final EntryTable table;

        public VirtualFragment(PopCount clop, EntryTable table) {
            super(table);
            this.clop = clop;
            this.table = table;
        }

        @Override
        public EntryTable get(RingEntry rad) {
            return table.filter(e -> e.clop(rad).equals(clop));
        }

        @Override
        public String toString() {
            return String.format("F[%d]", table.size());
        }
    }

    public static Fragment of(PopCount clop, EntryTable table, EntryTables registry) {
        if (table.isEmpty())
            return EMPTY;

        if (table.size() == 1)
            return singleton(clop, table.get(0));

        Fragment fragment = new VirtualFragment(clop, table);

        // only pop(0:0) are cached
        if(registry==null)
            return fragment;

        //List<EntryTable> tables = AbstractRandomList.virtual(RADS, fragment::get);

        //registry.register(tables);

        //assert equals(fragment, tables);

        return new Fragment(table) {

            EntryTable[] fragments = new EntryTable[RADS];

            @Override
            public EntryTable get(RingEntry rad) {
                EntryTable fragment = fragments[rad.index];
                if(fragment==null) {
                    fragment = table.filter(e -> e.clop(rad).equals(clop));
                    fragment = registry.table(fragment);
                    fragments[rad.index] = fragment;
                }
                return fragment;
            }

            @Override
            public String toString() {
                return String.format("F[%d]", table.size());
            }
        };
    }

    private static boolean equals(Fragment fragment, List<EntryTable> tables) {
        for (RingEntry rad : Entries.RADIALS) {
            EntryTable t0 = fragment.get(rad);
            EntryTable t1 = tables.get(rad.index);
            assert t1.equals(t0);
        }
        return true;
    }

    public boolean equals(Object o) {
        if(o == this)
            return true;

        if(!(o instanceof Fragment))
            return false;

        Fragment f = (Fragment) o;
        if(!Objects.equals(table, f.table))
            return false;

        for (RingEntry rad : Entries.RADIALS) {
            EntryTable t0 = this.get(rad);
            EntryTable t1 = f.get(rad);

            if(!Objects.equals(t0, t1))
                return false;
        }

        return true;
    }
}
