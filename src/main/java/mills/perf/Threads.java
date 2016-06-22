package mills.perf;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 01.01.12
 * Time: 18:41
 */
public class Threads extends Performer {

    static void join(Thread t) {
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
    }

    void run(int count) {
        if(count>0) {
            Thread t = new Thread();
            t.start();
            run(count-1);
            join(t);
        }
    }

    public void run() {
        run(3);
    }

    public static void main(String ... args) throws InterruptedException {

        int duration = 10;

        if(args.length>0)
            Integer.parseInt(args[0]);

        Threads t = new Threads();

        t.perform(1000*duration);
    }
}
