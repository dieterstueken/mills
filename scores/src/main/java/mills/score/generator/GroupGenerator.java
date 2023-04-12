package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.GroupIndex;
import mills.index.PosIndex;

import java.util.Map;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * version:     $
 * created by:  d.stueken
 * created on:  11.04.2023 13:03
 * modified by: $
 * modified on: $
 */
public class GroupGenerator extends LayerGroup<IndexLayer> {

    static final Logger LOGGER = Logger.getLogger(GroupGenerator.class.getName());

    final GroupsGenerator groups;

    public GroupGenerator(GroupsGenerator groups, Player player, Map<PopCount, ? extends IndexLayer> group) {
        super(groups.pop, player, group);
        this.groups = groups;
    }

    public static GroupGenerator create(GroupsGenerator groups, GroupIndex index, Player player) {
        return new GroupGenerator(groups, player, IndexLayer.group(index, player));
    }

    MovingGroups groups() {
        LOGGER.log(Level.FINE, ()->String.format("MovingGroups: %s%c", pop, player.key()));

        ForkJoinTask<ClosingGroup> closingTask = GroupsGenerator.submit(this::closingGroup);

        return new MovingGroups(targetGroup(), closingTask.join());
    }

    PosIndex index(PopCount clop) {
        return group.get(clop).index();
    }

    private TargetSlices targetSlices(PopCount clop) {
        ScoreTarget scores = ScoreTarget.allocate(index(clop), player);
        return TargetGroup.newSlices(scores);
    }

    private TargetGroup targetGroup() {

        Map<PopCount, ? extends TargetSlices> group = LayerGroup.group(
                MovingGroup.clops(pop), this::targetSlices);

        return new TargetGroup(pop, player, group);
    }

    private ScoreSlices lost(PopCount clop) {
        return ScoreSlices.of(ConstSet.lost(index(clop), player));
    }

    ClosingGroup closingGroup() {
        if (player.count(pop) <= 3) {
            return ClosingGroup.closed(pop, player, this::lost);
        }

        Function<PopCount, ScoreTarget> scores = clop -> ScoreTarget.allocate(index(clop), player);
        GroupElevator elevator = elevator();
        return ClosingGroup.closed(pop, player, scores.andThen(elevator::elevate));
    }

    GroupElevator elevator() {
        Player next = this.player;
        PopCount down = pop.sub(player.pop);

        // possible swap.
        if(down.nw>down.nb || (down.isSym() && next==Player.Black)) {
            next = next.opponent();
            down = down.swap();
        }

        LayerGroup<ScoreMap> group = groups.generator.generate(down).load(next);
        return GroupElevator.create(group);
    }
}
