package mills.partitions;

import mills.bits.PopCount;
import mills.util.AbstractRandomArray;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 24.04.16
 * Time: 12:30
 */
abstract public class Partitions<P> extends AbstractRandomArray<PartitionTable<P>> {

    public Partitions() {
        super(100);
    }

    public PartitionTable<P> get(PopCount pop) {
        return get(pop.index);
    }
}
