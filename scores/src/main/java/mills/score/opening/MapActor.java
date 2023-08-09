package mills.score.opening;

import mills.bits.Clops;
import mills.util.IntActor;

import java.util.function.LongConsumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:33
 */
public class MapActor implements LongConsumer {

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

    final IntActor actor;

    final LongConsumer action;

    public MapActor(OpeningMap target) {
        this.target = target;
        this.actor = new IntActor(target) {
            @Override
            public int submit(int posIndex) {

                // if already set
                if(target.get(posIndex))
                    return 1;

                return super.submit(posIndex);
            }
        };

        this.action = target.isComplete() ? NOOP : this;
    }

    @Override
    public String toString() {
        return "MapActor{" + target + '}';
    }

    @Override
    public void accept(long i201) {
        int index = target.index.posIndex(i201);
        actor.submit(index);
    }

    public Clops clops() {
        return target.clops();
    }

    public LongConsumer getAction() {
        return action;
    }

    public void close() {
        if(actor!=null)
            actor.close();
    }

    public static MapActor open(OpeningMap map) {
        return new MapActor(map);
    }
}
