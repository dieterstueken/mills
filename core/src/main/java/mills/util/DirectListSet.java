package mills.util;

import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 26.08.22
 * Time: 19:24
 */
public interface DirectListSet<T extends Indexed> extends IndexedListSet<T> {

    @Override
    default int findIndex(int key) {
        if(key<0 || key>=size())
            return -1;

        assert get(key).getIndex() == key;

        return key;
    }

    @Override
    default DirectListSet<T> headSet(T toElement) {
        return headSet(lowerBound(toElement));
    }

    @Override
    DirectListSet<T> headSet(int toIndex);

    static boolean isDirect(List<? extends Indexed> values) {
        for (int i = 0; i < values.size(); i++) {
            Indexed value = values.get(i);
            if(value.getIndex()!=i)
                return false;
        }
        return true;
    }

    static <T extends Indexed> DirectListSet<T> of(T[] entries, IntFunction<? extends T> generator) {
        Arrays.setAll(entries, generator);
        return of(entries, entries.length);
    }

    static <T extends Indexed> DirectListSet<T> of(T[] entries) {
        return of(entries, entries.length);
    }

    static <T extends Indexed> DirectListSet<T> of(T[] entries, int size) {
        assert DirectListSet.isDirect(entries);
        return DirectArraySet.of(entries, size);
    }

    static boolean isDirect(Indexed[] values) {
        for (int i = 0; i < values.length; i++) {
            Indexed value = values[i];
            if(value.getIndex()!=i)
                return false;
        }
        return true;
    }

    static boolean isDirect(Indexed value) {
        return value.getIndex()!=0;
    }
}
