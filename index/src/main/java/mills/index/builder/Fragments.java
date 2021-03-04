package mills.index.builder;

import mills.ring.Entries;
import mills.ring.EntryTable;
import mills.ring.IndexedEntryTable;
import mills.ring.RingEntry;
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

    List<List<IndexedEntryTable>> fragments;

    Fragments(IndexedEntryTable root, List<List<IndexedEntryTable>> fragments) {
        this.root = root;
        this.fragments = fragments;
    }

    public List<IndexedEntryTable> get(RingEntry rad) {
        return fragments.get(rad.radix());
    }

    public String toString() {
        return String.format("f[%d]", root.size());
    }

    static final Fragments EMPTY = new Fragments(EntryTable.EMPTY, AbstractRandomArray.constant(81, List.of()));

    static Fragments of(IndexedEntryTable root, Tables registry) {
        if(root.isEmpty())
            return EMPTY;
        
        if(root.size()==1) {
            return new Fragments(root, AbstractRandomArray.constant(81, List.of(root)));
        }

        Map<List<IndexedEntryTable>, List<IndexedEntryTable>> fragset = new HashMap<>();

        var fragments = Entries.RADIALS.stream()
                .map(rad -> {
                    var group = root.stream()
                            .collect(Collectors.groupingBy(rad::clops));
                    var list = registry.tablesOf(group.values());
                    return list;
                })
                .map(tables -> fragset.computeIfAbsent(tables, UnaryOperator.identity()))
                .collect(Collectors.toUnmodifiableList());
        
        return new Fragments(root, fragments) {
            public String toString() {
                return String.format("f[%d:%d]", root.size(), fragset.size());
            }
        };
    }
}
