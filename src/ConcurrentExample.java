import java.util.concurrent.*;

public class ConcurrentExample {
    static int count = 0;

    // synchronized 锁住方法
    public synchronized static void increment() {
        count++;
    }

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                increment();
            }
        });

        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 10000; i++) {
                count++;
            }
        });

        for (int i = 0; i < 10000; i++) {
            count++;
        }

        t1.start();
        t2.start();

        t1.join();
        t2.join();

        System.out.println("最终 count 的值是: " + count);
    }
}