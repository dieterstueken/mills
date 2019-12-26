package mills.score.generator;

public class SlicesGroups {

    final LayerGroup<Slices<? extends MapSlice>> moved;

    final LayerGroup<Slices<? extends ScoreSlice>> closed;

    public SlicesGroups(ScoreGroups scores) {
        this.moved = scores.moved.map(ScoreMap::slices);
        this.closed = scores.closed.map(ScoreSet::slices);
    }
}
