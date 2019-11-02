package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Positions;
import mills.stones.Mover;
import mills.stones.Moves;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 30.10.19
 * Time: 18:23
 */
public class SliceElevator extends SliceProcessor {

    private Mover close;

    private Mover take;

    public SliceElevator(ScoreSlice source, SlicesGroup<MapSlice> target) {
        super(source, target);

        boolean jumps = target.jumps();

        boolean swap = target.player()!=Player.White;
        close = Moves.moves(jumps).mover(swap);

        take = Moves.TAKE.mover();
    }

    public void process(int index, long i201) {

        Player player = source.player();
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int free = Stones.stones(i201, Player.None);

        // taking from mill allowed?
        take.move(stay, move, free).normalize();
        take.analyze(this::taken);
    }

    void taken(long i201) {
        Player player = target.player();
        int stay = Stones.stones(i201, player.other());
        int move = Stones.stones(i201, player);
        int closed = Stones.closed(move);

        close.move(stay, move, closed).normalize();
        take.analyze(this::closed);
    }

    void closed(long i201) {

        PopCount clop = Positions.clop(i201);

        var slices = target.get(clop);
        int index = slices.posIndex(i201);
        MapSlice slice = slices.get(index);
        slice.propagate(index, i201, score);
    }

    static SlicesGroup<MapSlice> elevate(SlicesGroup<? extends ScoreSlice> source, SlicesGroup<MapSlice> target) {

        source.group().values().parallelStream()
                .filter(slices->!slices.clop().equals(PopCount.EMPTY))
                .flatMap(slices->slices.slices().stream())
                .forEach(slice->elevate(slice, target));

        return target;
    }

    static void elevate(ScoreSlice slice, SlicesGroup<MapSlice> target) {
        new SliceElevator(slice, target);
        //todo: process, close
    }
}
