package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProvider;
import mills.util.AbstractRandomList;
import mills.util.ArraySet;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 27.10.19
 * Time: 16:15
 */
public class SliceGroup {

    final PopCount pop;

    final Player player;

    final Map<PopCount, ScoreSlices> group;

    public SliceGroup(PopCount pop, Player player, Map<PopCount, ScoreSlices> group) {
        this.pop = pop;
        this.player = player;
        this.group = group;
    }

    public void processAll(Consumer<? super MapSlice> process) {
        group.values().stream().flatMap(slices->slices.slices.stream()).parallel().forEach(process);
    }

    public void close() {
        group.values().forEach(ScoreSlices::close);
    }

    //////////////////////////////////////////

    static SliceGroup open(FileGroup files, IndexProvider indexes, boolean create) {
        PopCount mclop = files.pop.mclop();

        List<ScoreSlices> maps = AbstractRandomList.preset(PopCount.CLOPS.size(), null);

        files.group(mclop::le).entrySet().parallelStream()
                .map(e->{
                    var index = indexes.build(files.pop, e.getKey());
                    return ScoreMap.open(index, files.player, e.getValue(), create);
                })
                .map(ScoreSlices::of)
                .forEach(slices->maps.set(slices.scores.clop().index, slices));

        return new SliceGroup(files.pop, files.player, ArraySet.mapOf(PopCount::get, maps, null));
    }
}
