package mills.index.builder;

import mills.ring.*;
import mills.util.AbstractRandomArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.02.21
 * Time: 22:40
 */
public class Fragments {

    final IndexedEntryTable root;

    final List<IndexedEntryTables> fragments;

    Fragments(IndexedEntryTable root, List<IndexedEntryTables> fragments) {
        this.root = root;
        this.fragments = fragments;
    }

    public List<IndexedEntryTable> get(RingEntry rad) {
        return fragments.get(rad.radix());
    }

    public String toString() {
        return String.format("f[%d]", root.size());
    }

    static final Fragments EMPTY = new Fragments(IndexedEntryTable.of(), AbstractRandomArray.constant(81, IndexedEntryTables.of()));

    static Fragments of(IndexedEntryTable root) {
        List<IndexedEntryTables> fragments = AbstractRandomArray.constant(81, IndexedEntryTables.of(root));
        return new Fragments(root, fragments);
    }

    static Fragments of(IndexedEntryTable root, TableRegistry registry) {
        if(root.isEmpty())
            return EMPTY;
        
        if(root.size()==1) {
            return Fragments.of(root);
        }

        Map<IndexedEntryTables, IndexedEntryTables> fragset = new HashMap<>();

        // build a IndexedEntryTable list for each radial.
        // different tables are unified by fragset.

        List<IndexedEntryTables> fragments = Entries.RADIALS.stream()
                // split root into groups of same clop for each rad
                .map(rad ->root.stream().collect(Collectors.groupingBy(rad::clops)))
                // each group is converted into a IndexedEntryTables
                .map(group -> registry.tablesOf(group.values()))
                // canonicalize different IndexedEntryTables (hashCode?)
                .map(tables -> fragset.computeIfAbsent(tables, UnaryOperator.identity()))
                .toList();
        
        return new Fragments(root, fragments) {
            public String toString() {
                return String.format("f[%d:%d]", root.size(), fragset.size());
            }
        };
    }
}
