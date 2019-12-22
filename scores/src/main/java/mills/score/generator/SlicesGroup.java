package mills.score.generator;

import mills.bits.PopCount;
import mills.score.Score;

import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:15
 */
public class SlicesGroup<Slice extends ScoreSlice> extends LayerGroup<Slices<? extends Slice>> implements AutoCloseable {

    static SlicesGroup<ScoreSlice> lost(LayerGroup<?> layers) {
        return SlicesGroup.create(layers, clop->ScoreMap.lost(layers.get(clop)).slices());
    }

    static SlicesGroup<ScoreSlice> open(FileGroup files) {

        if(files.pop().min()<3)
            return lost(files);

        return SlicesGroup.create(files, clop->ScoreMap.open(files.get(clop)).slices());
    }

    static SlicesGroup<MapSlice> create(FileGroup files) throws IOException {

        files.create();

        SlicesGroup<ScoreSlice> down = open(files.down());

        SlicesGroup<MapSlice> group = SlicesGroup.create(files, clop->ScoreMap.create(files.get(clop)).slices());

        return SliceElevator.elevate(down, group);
    }

    static <Slice extends ScoreSlice> SlicesGroup<Slice>
    create(LayerGroup<?> layers, Function<PopCount, Slices<? extends Slice>> newSlice) {
        SlicesGroup<Slice> group = new SlicesGroup<>(layers);
        group.addAll(layers.group().keySet().parallelStream().map(newSlice));
        return group;
    }

    private SlicesGroup(Layer layer) {
        super(layer);
    }

    SlicesGroup<Slice> addAll(Stream<Slices<? extends Slice>> stream) {
        stream.forEach(slices->group.put(slices.clop(), slices));
        return this;
    }

    private SlicesGroup(LayerGroup<?> layers, Function<PopCount, Slices<? extends Slice>> newSlice) {
        super(layers);

        layers.group().keySet().parallelStream().forEach(clop->{
            Slices<? extends Slice> slices = newSlice.apply(pop);
            group.put(clop, slices);
        });
    }

    private SlicesGroup(Layer layer, Stream<Slices<? extends Slice>> stream) {
        super(layer);
        stream.forEach(slices->group.put(slices.clop(), slices));
    }

    Stream<? extends Slice> slices() {
        return group.values().stream().flatMap(Slices::stream);
    }

    Stream<? extends Slice> slices(Score score) {
        return slices().filter(slice->slice.hasScores(score));
    }

    public int max() {
        int max = 0;
        for (Slices<? extends Slice> slice : group.values()) {
            max = Math.max(0, slice.max());
        }
        return max;
    }

    public void close() {
        group.values().forEach(Slices::close);
    }
}
