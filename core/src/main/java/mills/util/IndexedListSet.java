package mills.util;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  26.08.2022 15:42
 * modified by: $
 * modified on: $
 */
public interface IndexedListSet<T extends Indexed> extends ListSet<T> {

    int findIndex(int ringIndex);
}
