package mills.scores.opening;

import mills.position.Position;
import mills.position.Situation;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 02.11.13
 * Time: 15:45
 */
abstract public class SliceTarget implements Position.Factory {

    final Situation situation;

    protected SliceTarget(Situation situation) {
        this.situation = situation;
    }

    public Situation situation() {
        return situation;
    }

    @Override
    public Position position(long i201) {
        return situation.position(i201);
    }

    abstract public int apply(long i201);
}
