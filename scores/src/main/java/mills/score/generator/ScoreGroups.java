package mills.score.generator;

public class ScoreGroups {

    final LayerGroup<ScoreMap> moved;

    final LayerGroup<? extends ScoreSet> closed;

    public ScoreGroups(LayerGroup<ScoreMap> moved, LayerGroup<? extends ScoreSet> closed) {
        this.moved = moved;
        this.closed = closed;
    }
}
