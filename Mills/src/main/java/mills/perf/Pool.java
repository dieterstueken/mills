package mills.perf;

import java.util.concurrent.RecursiveAction;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.01.12
 * Time: 18:41
 */
public class Pool extends Performer {

    static RecursiveAction action(final int count) {

        return new RecursiveAction() {

            @Override
            protected void compute() {
                if(count>0) {
                    RecursiveAction f = action(count-1);
                    f.fork();
                    f.join();
                }
            }
        };
    }

    public void run() {
        action(3).fork();
        //pool.invoke(action(3));
    }

    public static void main(String ... args) throws InterruptedException {

        int duration = 10;

        if(args.length>0)
            Integer.parseInt(args[0]);

        Pool t = new Pool();

        t.perform(1000*duration);
    }
}
