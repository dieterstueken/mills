package mills.index2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomList;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.10.19
 * Time: 20:34
 */
public class Partitions {

    final List<Partition> partitions;

    public Partitions(List<Partition> partitions) {
        this.partitions = partitions;
    }

    public Partition get(PopCount pop) {
        int index = pop.index;
        if(index < partitions.size())
            return partitions.get(index);
        else
            return Partition.EMPTY;
    }

    public static Partitions build(EntryTable root, EntryTables registry) {

        AbstractRandomList<Partition> partitions = AbstractRandomList.preset(PopCount.TABLE.size(), null);

        PopCount.TABLE.stream()
                .filter(p->p.max()<=8)
                .forEach(pop -> partitions.set(pop.index, Partition.build(root.filter(pop.eq), registry)));

        return new Partitions(partitions);
    }
}
