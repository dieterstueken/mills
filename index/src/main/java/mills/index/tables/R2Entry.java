package mills.index.tables;

import mills.ring.RingEntry;
import mills.util.Indexer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 05.10.19
 * Time: 20:31
 */
public class R2Entry {

    public static final Indexer<R2Entry> R2 = element -> element.e2.index;

    final RingEntry e2;

    final R0Table t0;

    public R2Entry(RingEntry e2, R0Table t0) {
        this.e2 = e2;
        this.t0 = t0;
    }

    public RingEntry r2() {
        return e2;
    }

    public R0Table t0() {
        return t0;
    }

    @Override
    public String toString() {
        return String.format("%s : %d", e2, t0.size());
    }
}
