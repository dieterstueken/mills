package mills.index;

import mills.bits.PopCount;
import mills.util.PopMap;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.02.23
 * Time: 14:43
 */
public interface GroupIndex extends PosIndex {

    PopMap<? extends PosIndex> group();

    default PosIndex getIndex(PopCount clop) {
        return clop == null ? this : group().get(clop);
    }
}
