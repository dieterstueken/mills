package mills.score.opening;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 12.11.19
 * Time: 22:05
 */
public class CloseLayer extends PlopLayer {

    final MoveLayer moves;

    CloseLayer(MoveLayer moves) {
        super(moves);
        this.moves = moves;
    }

    void elevate(MoveLayer target) {

    }
}
