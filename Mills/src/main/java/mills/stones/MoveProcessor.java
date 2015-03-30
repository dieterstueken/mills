package mills.stones;

/**
* Created by IntelliJ IDEA.
* User: stueken
* Date: 06.01.14
* Time: 11:38
*/
public interface MoveProcessor {

    boolean process(int stay, int move);

    MoveProcessor ANY = new MoveProcessor() {
        @Override
        public boolean process(int stay, int move) {
            return true;
        }

        public String toString() {
            return "ANY";
        }
    };

    MoveProcessor NONE = new MoveProcessor() {
            @Override
            public boolean process(int stay, int move) {
                return false;
            }

            public String toString() {
                return "NONE";
            }
        };

}
