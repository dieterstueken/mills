package mills.score.opening;

import mills.index.PosIndex;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 06.08.23
 * Time: 18:11
 */
abstract public class OpeningMap extends OpeningIndex {

    OpeningMap(PosIndex index, int turn) {
        super(index, turn);
    }

    abstract public boolean get(long i201);

    /**
     * @return return a complete OpeningIndex if this OpeningIndex has all bits set.
     */
    abstract public OpeningMap complete();

    /**
     * @param index
     * @param turn
     * @return a filled OpeningIndex with all bits set.
     */
    public static OpeningMap complete(PosIndex index, int turn) {
        return new OpeningMap(index, turn) {

            @Override
            public boolean get(final long i201) {
                return true;
            }

            @Override
            public OpeningMap complete() {
                return this;
            }
        };
    }
}
