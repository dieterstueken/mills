package mills.index2;

import mills.index.R2Entry;
import mills.index.R2Table;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 5/29/14
 * Time: 4:57 PM
 */
public class MopIndex extends R2Table {

    public final Mop mop;

    public Mop mop() {
        return mop;
    }

    public MopIndex(Mop mop, final List<R2Entry> table, final List<R2Entry> entries) {
        super(mop.count, table, entries);
        this.mop = mop;
    }


}
