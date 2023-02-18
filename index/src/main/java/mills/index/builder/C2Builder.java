package mills.index.builder;

import mills.bits.PopCount;
import mills.index.tables.R0Table;
import mills.ring.EntryMap;
import mills.ring.EntryTable;
import mills.ring.RingEntry;

import static java.util.function.Predicate.not;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 15:54
 */
class C2Builder {

    final PopCount clop;

    final EntryMap<R0Table> t0Tables;

    C2Builder(PopCount clop, EntryTable t2) {
        this.clop = clop;
        this.t0Tables = EntryMap.preset(t2, R0Table.EMPTY);
    }

    void put(RingEntry r2, R0Table t0Table) {
        t0Tables.put(r2, t0Table);
    }

    EntryMap<R0Table> tables() {
        return t0Tables.filterValues(not(R0Table::isEmpty));
    }
}
