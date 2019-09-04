package mills.index3.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;

import java.util.Collection;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 20:12
 * modified by: $Author$
 * modified on: $Date$
 */
public class Partitions extends PartitionTable<MaskTable> {

    final List<MaskTable> partitions;

    final List<EntryTable> lePop;

    public final EntryTables registry;

    final Collection<MaskTable> content;

    public Partitions(EntryTables registry, List<MaskTable> partitions, Collection<MaskTable> content, List<EntryTable> lePop) {
        this.registry = registry;
        this.partitions = partitions;
        this.content = content;
        this.lePop = lePop;
    }

    @Override
    public MaskTable get(int index) {
        return partitions.get(index);
    }

    @Override
    public int size() {
        return 100;
    }

    public EntryTable lePop(PopCount pop) {
        return pop==null ? EntryTable.EMPTY : lePop.get(pop.index);
    }

    @Override
    public Collection<MaskTable> content() {
        return content;
    }

    public MaskTable get(PopCount pop) {
        return get(pop.index);
    }

    public static Partitions build(EntryTables registry) {
        return new Builder(registry).partitions();
    }
}
