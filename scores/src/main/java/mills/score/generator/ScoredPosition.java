package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Position;
import mills.position.Positions;

class ScoredPosition extends Position implements Layer {

    final int score;

    final ScoredPosition normalized;

    @Override
    protected Position position(long i201) {
        return position(Positions.inverted(i201), player, score);
    }

    protected ScoredPosition position(long i201, Player player, int score) {
        return new ScoredPosition(i201, player, score);
    }

    protected ScoredPosition(long i201, Player player, int score) {
        super(i201, player);
        this.score = score;

        //posIndex = scores.index.posIndex(player==scores.player()? i201 : j201);
        //score = posIndex < 0 ? -1 : scores.getScore(posIndex);

        if (super.isNormalized)
            normalized = this;
        else
            normalized = position(Positions.normalize(i201), player, score);

        //this.inverted = inverted;//!=null ? inverted : position(j201, player.other(), score, this);
    }

    public ScoredPosition inverted() {
        return new ScoredPosition(Positions.inverted(i201), player.opponent(), score) {
            @Override
            public ScoredPosition inverted() {
                return ScoredPosition.this;
            }
        };
    }

    @Override
    public StringBuilder format(StringBuilder sb) {
        sb = super.format(sb);
        sb.insert(3, player.key());
        //sb.append("[").append(posIndex).append("]: ");
        sb.append(':');
        sb.append(score);
        return sb;
    }

    @Override
    public PopCount pop() {
        return pop;
    }

    @Override
    public Player player() {
        return player;
    }
}
