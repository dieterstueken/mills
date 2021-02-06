package mills.index2.builder;

import mills.bits.PopCount;
import mills.index.tables.C2Table;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.02.21
 * Time: 09:07
 */
public class IndexTables {

    final Map<PopCount, C2Table> tables;

    IndexTables(Map<PopCount, C2Table> tables) {
        this.tables = tables;
    }
}
