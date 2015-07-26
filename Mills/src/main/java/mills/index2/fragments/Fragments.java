package mills.index2.fragments;

import mills.bits.PopCount;
import mills.ring.EntryTable;
import mills.util.AbstractRandomList;

import java.util.Arrays;
import java.util.List;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  24.07.2015 18:00
 * modified by: $Author$
 * modified on: $Date$
 */
abstract public class Fragments extends AbstractRandomList<EntryTable> {

    public int size() {
        return 25;
    }

    abstract public int clopMask();

    public EntryTable get(PopCount clop) {
        return get(clop.index);
    }

    public static final Fragments EMPTY = new Fragments() {
        @Override
        public int clopMask() {
            return 0;
        }

        @Override
        public EntryTable get(int index) {
            return EntryTable.EMPTY;
        }
    };

    static Fragments of(PopCount clop, EntryTable source) {
        return new Fragments() {
            @Override
            public EntryTable get(int index) {
                return index==clop.index ? source : EntryTable.EMPTY;
            }
            @Override
            public int clopMask() {
                return 1<<clop.index;
            }
        };
    }

    static Fragments of(byte clops[], int msk, List<EntryTable> frags) {

        return new Fragments() {
            @Override
            public int clopMask() {
                return msk;
            }

            @Override
            public EntryTable get(int index) {

                if((msk & 1<<index)==0)
                    return EntryTable.EMPTY;

                // if msk is correct i should not be negative
                int i = Arrays.binarySearch(clops, (byte) index);
                return frags.get(i);
            }
        };
    }
}
