package mills.index;

import mills.bits.Clops;
import mills.bits.PopCount;
import mills.position.Position;
import mills.position.Positions;
import mills.util.AbstractRandomArray;
import mills.util.listset.PopMap;

import java.util.List;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 16.09.2010
 * Time: 12:17:35
 */

public interface PosIndex extends Clops {

    PosIndex root();

    PopMap<? extends PosIndex> group();

    default PosIndex getIndex(PopCount clop) {
        return clop == null ? root() : group().get(clop);
    }

    PopCount pop();

    default PopCount clop() {
        return null;
    }

    int range();

    int posIndex(long i201);

    long i201(int posIndex);

    default long normalize(long i201) {
        return Positions.normalize(i201);
    }

    default Position position(int posIndex) {
        return Position.of(i201(posIndex));
    }

    default boolean isEmpty() {
        return range()==0;
    }

    // run processor for a given range
    IndexProcessor process(IndexProcessor processor, int start, int end);

    default IndexProcessor process(IndexProcessor receiver) {
        return process(receiver, 0, Integer.MAX_VALUE);
    }

    int n20();

    default boolean verify(long i201) {
        PopCount p = Positions.pop(i201);
        return p == pop();
    }

    /**
     * Debug:
     * @return virtual table of positions
     */
    default List<Position> positions() {
        return AbstractRandomArray.virtual(range(), this::position);
    }

    /**
     * The empty layer can be synthesized.
     * It contains the empty position as a single entry.
     */
    PosIndex EMPTY = new PosIndex() {

        final List<Position> positions = List.of(Position.EMPTY);

        final PopMap<PosIndex> group = PopMap.of(PopCount.EMPTY, this);

        @Override
        public PosIndex root() {
            return this;
        }

        @Override
        public PopMap<? extends PosIndex> group() {
            return group;
        }

        @Override
        public PopCount pop() {
            return PopCount.EMPTY;
        }

        @Override
        public int range() {
            return 1;
        }

        @Override
        public int posIndex(final long i201) {
            if(Positions.m201(i201)!=0)
                throw new NoSuchElementException("PosIndex.EMPTY");
            return 0;
        }

        @Override
        public long i201(final int posIndex) {
            if(posIndex!=0)
                throw new IndexOutOfBoundsException("PosIndex.EMPTY");
            return Positions.NORMALIZED;
        }

        @Override
        public IndexProcessor process(final IndexProcessor processor, final int start, final int end) {
            // process empty element ef requested
            if(start==0 && end>0)
                processor.process(0, Positions.NORMALIZED);

            return processor;
        }

        @Override
        public int n20() {
            return 1;
        }

        @Override
        public Position position(final int posIndex) {
            return positions.get(posIndex);
        }

        @Override
        public boolean isEmpty() {
            return PosIndex.super.isEmpty();
        }

        @Override
        public IndexProcessor process(final IndexProcessor receiver) {
            return PosIndex.super.process(receiver);
        }

        @Override
        public boolean verify(final long i201) {
            return PosIndex.super.verify(i201);
        }

        @Override
        public List<Position> positions() {
            return PosIndex.super.positions();
        }
    };
}
