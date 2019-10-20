package mills.index.builder;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 13.10.19
 * Time: 11:05
 */
public class Fragments {

    static final Fragments EMPTY = new Fragments() {
        @Override
        public String toString() {
            return "F[]";
        }
    };

    private final EntryTable root;

    private final Map<PopCount, Fragment> fragments;

    private final Set<EntryTable> roots;

    private Fragments() {
        this.fragments = Collections.emptyMap();
        this.roots = EntryTable.EMPTY.singleton();
        this.root =  EntryTable.EMPTY;
    }

    Fragments(Map<PopCount, Fragment> fragments, EntryTable root, Set<EntryTable> roots) {
        this.fragments = fragments;
        this.root = root;
        this.roots = roots;
    }

    public EntryTable root() {
        return root;
    }

    public EntryTable get(PopCount clop, RingEntry rad) {
        Fragment fragment = fragments.get(clop);
        
        if (fragment == null)
            return EntryTable.EMPTY;

        return fragment.get(rad);
    }

    public String toString() {
        return String.format("F[%d+%d]", fragments.size(), roots.size());
    }
}
