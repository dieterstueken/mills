package mills.partitions;

import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/15/15
 * Time: 1:38 PM
 */
public class Partition {

    public static final List<Partition> TABLE = AbstractRandomList.generate(128, new Generator());

    public final EntryTable root;

    public final List<EntryTable> partitions;

    final EntryTables registry;

    Partition(EntryTable root, List<EntryTable> partitions, EntryTables registry) {
        this.root = root;
        this.registry = registry;
        this.partitions = partitions;
    }

    public EntryTable pop(int pop) {
        if(pop<0 || pop>=partitions.size())
            return EntryTable.EMPTY;

        return partitions.get(pop);
    }

    public String toString() {
        return String.format("%3d", root.size());
    }
}
