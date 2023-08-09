package mills.score.opening;

import mills.bits.Clops;
import mills.util.QueueActor;

import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:33
 */
public class MapActor {

    static final LongConsumer NOOP = new LongConsumer() {
        @Override
        public void accept(long i201) {}

        @Override
        public String toString() {
            return "NOOP";
        }
    };

    // this is the player on Target
    final OpeningMap target;

    final QueueActor actor;

    final LongConsumer action;

    public MapActor(OpeningMap target) {
        this.target = target;
        this.actor = new QueueActor();
        this.action = target.isComplete() ? NOOP : i201 -> actor.submit(()->target.set(i201));
    }

    public Clops clops() {
        return target.clops();
    }

    public void set(long i201) {
        action.accept(i201);
    }

    public void close() {
        if(actor!=null)
            actor.close();
    }

    public static MapActor open(OpeningMap map) {
        return new MapActor(map);
    }
}
