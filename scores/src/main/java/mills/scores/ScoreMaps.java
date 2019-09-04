package mills.scores;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.12.12
 * Time: 00:49
 */
public class ScoreMaps extends Pair<ScoreMap> implements Closeable {

    public ScoreMaps(ScoreMap self, ScoreMap other) {
        super(self, other);
    }

    public static ScoreMaps of(ScoreMap self, ScoreMap other) {
        return new ScoreMaps(self, other);
    }

    public static ScoreMaps of(Pair<ScoreMap> maps) {
        return of(maps.self, maps.other);
    }

    public ScoreMaps swap() {

        if(equal())
            return this;

        return new ScoreMaps(other, self);
    }

    public void close() throws IOException {
        self.close();
        if(!equal())
            other.close();
    }

    public Position position(long i201) {
        return new Position(i201);
    }

    public class Position extends ScoreMap.Position {

        public Position(long i201) {
            self.super(i201);
        }

        public Position position(long i201) {
            return ScoreMaps.this.position(i201);
        }
    }
}
