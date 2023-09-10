package mills.score.opening;

import mills.bits.IClops;
import mills.util.IntActor;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 29.08.22
 * Time: 16:33
 */
public class MapActor {

    // this is the player on Target
    final OpeningMap target;

    public MapActor(OpeningMap target) {
        this.target = target;
    }

    @Override
    public String toString() {
        return "MapActor{" + target + '}';
    }

    public void set(long i201) {
    }

    public IClops clops() {
        return target.clops();
    }

    public void close() {
    }

    public static MapActor open(OpeningMap target) {
        if(target.isComplete())
            return new MapActor(target);
        else {
            return new MapActor(target) {

                final IntActor actor = new IntActor(target::set) {
                    @Override
                    public int submit(int posIndex) {

                        // if already set
                        if(target.get(posIndex))
                            return 1;

                        return super.submit(posIndex);
                    }
                };

                public void set(long i201) {
                    int index = target.index.posIndex(i201);
                    actor.submit(index);
                }

                public void close() {
                    actor.close();
                }
            };
        }
    }
}
