package mills.score;

import mills.bits.Player;
import mills.index.IndexProcessor;
import mills.stones.Stones;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 19.05.13
 * Time: 16:56
 */
abstract class ScoreElevator extends ScoreWorker {

    final IndexProcessor stuck;

    ScoreElevator(ScoreSlices source, ScoreSlices target) {
        super(source, target);
        this.stuck = source.stuck();
    }

    protected SliceElevator processor() {
        return new SliceElevator();
    }

    protected void processSlice(IndexProcessor processor, ScoreSlice slice) {
        slice.processAll(processor);
    }

    interface Elevator {
        int elevate(long i201);
    }

    abstract Elevator elevator();

    class SliceElevator extends Processor {

        final Move open = Move.reverse(source.map, target.map);

        final Elevator elevator = elevator();

        int score = 0;

        @Override
        public boolean analyze(final ScoreSlice slice, short offset, long i201) {

            // try to open any mill
            final int size = open.close(i201).size();

            if (size == 0) {
                // no closings, any stuck?
                if (stuck != null)
                    stuck.process(slice.posIndex(offset), i201);

                return false;
            }

            score = elevator.elevate(i201)+1;

            if(score==1)
                open.analyze(count);
            else if(Score.isWon(score))
                open.analyze(won);
            else
                open.analyze(lost);

            return true;
        }

        abstract class Analyzer implements Move.Analyzer {

            public void analyze(final long i201) {

                final int index = target.map.posIndex(i201);
                final ScoreSlice slice = target.getSlice(index);
                final short offset = slice.offset(index);

                propagate(slice, offset, i201, score);
            }

            abstract boolean resolved(ScoreSlice slice, short offset, int score);

            void propagate(final ScoreSlice slice, final short offset, long i201, final int score) {
                if(resolved(slice,offset, score))
                    return;

                slice.submit(s -> {
                    if(!resolved(s, offset, score))
                        s.setScore(offset, score);
                });
            }
        }

        final Analyzer count = new Analyzer() {

            final Move count = Move.forward(target.map, source.map);

            void propagate(final ScoreSlice slice, final short offset, long i201, int score) {
                score = count.level(i201).size();

                if(score!=0)
                    super.propagate(slice, offset, i201, score);
            }

            @Override
            boolean resolved(ScoreSlice slice, short offset, int score) {
                final int current = slice.getScore(offset);

                if(current==score || Score.isWon(current))
                    return true;        // leave if done or any won

                if(current==0)          // replace any empty score
                    return false;

                if(current>0)           // terminate any loss path by an indifferent
                    return false;

                // something went wrong

                String error = String.format("%s: indifferent results on %s %d/%d: %d x %d",
                        ScoreElevator.this.toString(), slice, offset, slice.posIndex(offset), current, score);

                throw new IllegalStateException(error);
            }
        };

        final Analyzer won = new Analyzer() {

            @Override
            boolean resolved(ScoreSlice slice, short offset, int score) {
                final int current = slice.getScore(offset);
                // only shorter win will stay
                return Score.isWon(current) && current <= score;
            }
        };

        final Analyzer lost = new Analyzer() {

            @Override
            boolean resolved(ScoreSlice slice, short offset, int score) {
                final int current = slice.getScore(offset);

                if(current==score || Score.isWon(current)) // leave if done or any won
                    return true;

                if(current==0)          // replace any empty score
                    return false;

                // leave any longer or even indifferent
                return current>=score || current < 0;
            }
        };
    }

    public static ScoreElevator of(ScoreSlices source, ScoreSlices target, final ScoreMap downMap) {
        if (downMap == null)
            return new ScoreElevator(source, target) {

                public String name() {
                    return "ScoreElevator";
                }

                final Elevator lost = new Elevator() {

                    public int elevate(long i201) {

                        assert mayTake(i201);

                        return Score.LOST;
                    }

                    // Debug
                    private boolean mayTake(long i201) {

                        final ScoreMap map = source.map;

                        final Player player = map.player();

                        // see if opponent has closed any mill
                        int other = Stones.stones(i201, player.other());
                        final int closed = Stones.closed(other);

                        if (closed != 0) {
                            int self = Stones.stones(i201, player);

                            if (map.moves(player.other()).any(self, other, closed))
                                return true;
                        }

                        return false;
                    }
                };

                @Override
                Elevator elevator() {
                    return lost;
                }
            };
        else
            return new ScoreElevator(source, target) {

                public String name() {
                    return String.format("ScoreElevator [%s]", downMap.toString());
                }

                @Override
                Elevator elevator() {

                    return new Elevator() {

                        final Move take = Move.take(source.map, downMap);

                        @Override
                        public int elevate(long i201) {
                            final int size = take.move(i201).size();

                            if (size == 0)
                                return 0;

                            int score = Score.LOST;

                            for (int i = 0; i < size; ++i) {
                                long t201 = take.get201(i);
                                int downIndex = downMap.posIndex(t201);
                                int s = downMap.getScore(downIndex);
                                if(Score.betterThan(s, score))
                                    score = s;
                            }

                            return score;
                        }
                    };
                }
            };
    }
}
