package mills.main;

import mills.bits.Player;
import mills.bits.PopCount;
import mills.index.IndexProcessor;
import mills.index.IndexProvider;
import mills.index.PosIndex;
import mills.position.Positions;
import mills.stones.Stones;
import mills.util.AbstractRandomList;

import java.util.List;
import java.util.concurrent.RecursiveAction;

/**
 * version:     $Revision$
 * created by:  dst
 * created on:  12.12.12 20:59
 * modified by: $Author$
 * modified on: $Date$
 */
public class VerifyIndex {

    static final int SIZE = Short.MAX_VALUE;

    protected final IndexProvider indexes = IndexProvider.load();

    public static void main(String... args) {

        VerifyIndex main = new VerifyIndex();
        main.run();
    }

    void run() {

        //verify(PopCount.of(9,9));

        for (PopCount pop : PopCount.TABLE) {
            System.out.println(pop);
            verify(pop);
        }
    }

    IndexProcessor processor(PosIndex index) {
        return (posIndex, i201) -> {

            for (int i = 0; i < 16; i++) {
                long p201 = Positions.permute(i201, i);
                long n201;

                n201 = index.normalize(p201);

                int black = Stones.stones(p201, Player.Black);
                int white = Stones.stones(p201, Player.White);
                long m201 = Stones.i201(black, white);

                if (!Positions.equals(i201, n201) || !Positions.equals(m201, p201)) {
                    System.err.format("index %d perm %d\n", posIndex, i);
                    System.err.format("i %016x %s\n", i201, Positions.position(i201));
                    System.err.format("p %016x %s\n", p201, Positions.position(p201));
                    System.err.format("n %016x %s\n", n201, Positions.position(n201));
                    System.err.format("m %016x %s\n", m201, Positions.position(m201));

                    n201 = index.normalize(p201);

                    throw new RuntimeException();
                }

            }
        };
    }

    private void verify(final PopCount pop) {

        RecursiveAction task = new RecursiveAction() {
            @Override
            protected void compute() {

                final PosIndex index = indexes.build(pop);

                final IndexProcessor processor = processor(index);

                final int size = (index.range() + SIZE - 1) / SIZE;

                List<RecursiveAction> tasks = AbstractRandomList.generate(size, i->{
                        final int start = i * SIZE;
                        final int end = Math.min(start+SIZE, index.range());

                        return new RecursiveAction() {

                            @Override
                            protected void compute() {
                                index.process(processor, start, end);
                            }
                        };
                    }
                );

                invokeAll(tasks);
            }
        };

        task.invoke();
    }
}
