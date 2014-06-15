package mills.perf;

/**
 * Created by IntelliJ IDEA.
 * User: stueken
 * Date: 03.12.11
 * Time: 16:16
 */
public abstract class Performer implements Runnable {

    class Runner extends Thread {

        volatile boolean stop = false;

        long count=0;
        long spend=0;

        public void run() {
            long start = System.currentTimeMillis();
            long i=0;
            while(!stop) {
                Performer.this.run();
                i++;
            }

            long stop = System.currentTimeMillis();
            count = i;
            spend = stop - start;
        }
    }

    public void perform(int interval) throws InterruptedException {

        Runner r = new Runner();
        r.start();

        try {
            Thread.sleep(interval);
        } finally {
            r.stop = true;
        }

        r.join();

        System.out.format("%d calls in %d ms: %.1f µs/call\n", r.count, r.spend, 1000.0 * r.spend / r.count);
    }

    public static void main(String ... args) throws InterruptedException {

        Performer p = new Performer() {

            @Override
            public void run() {
                Thread t = new Thread();
                t.start();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        };

        p.perform(1000);
    }

}
