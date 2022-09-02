package mills.index.builder;

import mills.bits.PopCount;
import mills.util.CachedBuilder;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.09.22
 * Time: 19:27
 */
abstract public class IndexBuilder extends CachedBuilder<IndexGroup> {

    final PopCount pop;

    public IndexBuilder(PopCount pop) {
        this.pop = pop;
    }

    @Override
    public String toString() {
        return "IndexBuilder(" + pop + ')';
    }

    public PopCount pop() {
        return pop;
    }
}
