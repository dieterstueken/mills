package mills.score.generator;

import mills.bits.PopCount;
import mills.util.ArraySet;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.11.19
 * Time: 20:59
 */
public class Group<T> {

    final Map<PopCount, T> group;
    
    public Group() {
        this.group = ArraySet.mapOf(PopCount.CLOPS, null);
    }

    public Map<PopCount, T> group() {
        return group;
    }

    public T group(PopCount clop) {
        return group.get(clop);
    }
}
