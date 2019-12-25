package mills.score.generator;

import mills.bits.Player;
import mills.position.Position;
import mills.position.Positions;

class ScoredPosition extends Position {

    final ScoreSet scores;

    final Player player;
    final int posIndex;
    final int score;

    final ScoredPosition normalized;
    final ScoredPosition inverted;

    public ScoredPosition position(long i201, Player player) {
        return scores.position(i201, player);
    }

    public ScoredPosition position(long i201) {
        return position(i201, this.player);
    }

    public ScoredPosition(ScoreSet scores, long i201, Player player) {
        super(i201);
        this.scores = scores;
        this.player = player;

        long j201 = Positions.inverted(i201);

        posIndex = scores.index.posIndex(player==scores.player()? i201 : j201);
        score = posIndex < 0 ? -1 : scores.getScore(posIndex);

        if (super.isNormalized)
            normalized = this;
        else
            normalized = scores.position(Positions.normalize(i201), player);

        inverted = scores.position(j201, player.other());
    }

    @Override
    public StringBuilder format(StringBuilder sb) {
        sb = super.format(sb);
        sb.insert(3, player.key());
        sb.append("[").append(posIndex).append("]: ");
        sb.append(score);
        return sb;
    }
}
