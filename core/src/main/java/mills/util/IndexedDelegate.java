package mills.util;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.09.22
 * Time: 17:32
 */
abstract public class IndexedDelegate<T> extends DelegateListSet<T> implements IndexedListSet<T>  {

    public IndexedDelegate(List<T> values) {
        super(values);
    }

}
