package mills.scores.opening2;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 04.01.14
 * Time: 16:35
 */
public interface BitMap {

    public boolean set(int posIndex);

    public boolean get(int posIndex);

    public static BitMap FULL = new BitMap() {

        @Override
        public boolean set(int posIndex) {
            throw new UnsupportedOperationException("read only BitMap");
        }

        @Override
        public boolean get(int posIndex) {
            return true;
        }

        public String toString() {
            return "FULL";
        }
    };

    public static BitMap EMPTY = new BitMap() {

        @Override
        public boolean set(int posIndex) {
            throw new UnsupportedOperationException("read only BitMap");
        }

        @Override
        public boolean get(int posIndex) {
            return false;
        }


        public String toString() {
            return "EMPTY";
        }
    };
}
