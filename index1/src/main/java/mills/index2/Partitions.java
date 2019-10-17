package mills.index2;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.ring.EntryTables;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 17.10.19
 * Time: 20:34
 */
public class Partitions {

    final List<Partition> partitions;

    final Map<PopCount, Partition> map;

    public Partitions(List<Partition> partitions) {
        this.partitions = partitions;
        this.map = new TreeMap<>();
        for (PopCount pop : PopCount.TABLE) {
            Partition partition = partitions.get(pop.index);
            if(partition!=null)
                map.put(pop, partition);
        }
    }

    public Partition get(PopCount pop) {
        int index = pop.index;
        Partition partition = index<partitions.size() ? partitions.get(index) : Partition.EMPTY;
        return partition!=null ? partition : Partition.EMPTY;
    }

    public static Partitions build(EntryTable root, EntryTables registry) {

        AbstractRandomList<Partition> partitions = AbstractRandomList.preset(PopCount.TABLE.size(), null);

        PopCount.TABLE.stream()
                .filter(p->p.max()<=8)
                .forEach(pop -> partitions.set(pop.index, Partition.build(root.filter(pop.eq), registry)));

        return new Partitions(partitions);
    }
}
