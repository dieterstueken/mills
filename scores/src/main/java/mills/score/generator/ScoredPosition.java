package mills.score.generator;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.position.Position;
import mills.position.Positions;

class ScoredPosition extends Position implements Layer {

    final Player player;
    final int score;

    final ScoredPosition normalized;
    final ScoredPosition inverted;

    protected ScoredPosition position(long i201, Player player, int score, ScoredPosition inverted) {
        return new ScoredPosition(i201, player, score, inverted);
    }

    public ScoredPosition(long i201, Player player, int score, ScoredPosition inverted) {
        super(i201);
        this.player = player;
        this.score = score;

        long j201 = Positions.inverted(i201);

        //posIndex = scores.index.posIndex(player==scores.player()? i201 : j201);
        //score = posIndex < 0 ? -1 : scores.getScore(posIndex);

        if (super.isNormalized)
            normalized = this;
        else
            normalized = position(Positions.normalize(i201), player, score, null);

        this.inverted = inverted!=null ? inverted : position(j201, player.other(), score, this);
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
