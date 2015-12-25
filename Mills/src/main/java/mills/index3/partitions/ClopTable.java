package mills.index3.partitions;

import mills.bits.PopCount;
import mills.ring.EntryTable;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:00
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class ClopTable {

    public void forEach(BiConsumer<? super PopCount,? super EntryTable> action) {
        content().forEach(action);
    }

    abstract Map<PopCount, EntryTable> content();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ClopTable && Objects.equals(content(), ((ClopTable)obj).content());
    }

    @Override
    public int hashCode() {
        return content().hashCode();
    }

    public static final ClopTable EMPTY = new ClopTable() {

        Map<PopCount, EntryTable> content() {
            return Collections.emptyMap();
        }

        @Override
        public String toString() {
            return "empty";
        }
    };

    public static ClopTable of(Map<PopCount, EntryTable> content) {
        return new ClopTable() {

            @Override
            Map<PopCount, EntryTable> content() {
                return content;
            }
        };
    }
}
