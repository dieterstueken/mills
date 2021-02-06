package mills.index2.builder;

import mills.bits.PopCount;
import mills.ring.Entries;
import mills.util.AbstractRandomList;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 09:04
 */
public class IndexBuilder {

    final Partitions partitions = new Partitions();

    final PopTable eqPops = new PopTable(AbstractRandomList.transform(PopCount.TABLE, pop -> Entries.MINIMIZED.filter(pop.eq)));

    final PopTable lePops = new PopTable(AbstractRandomList.transform(PopCount.TABLE, pop -> Entries.TABLE.filter(pop.le)));

    public IndexBuilder() {
    }




}
