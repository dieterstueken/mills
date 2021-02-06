package mills.index2.builder;

import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 10:16
 */
public class PopTable {

    final List<EntryTable> tables;

    public PopTable(List<EntryTable> tables) {
        this.tables = tables;
    }

    public EntryTable get(PopCount pop) {
        return tables.get(pop.index);
    }

}
