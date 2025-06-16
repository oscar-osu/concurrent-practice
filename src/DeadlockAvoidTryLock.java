import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.TimeUnit;

public class DeadlockAvoidTryLock {
    private static final ReentrantLock lockA = new ReentrantLock();
    private static final ReentrantLock lockB = new ReentrantLock();

    public static void main(String[] args) {
        Thread t1 = new Thread(() -> {
            try {
                if (lockA.tryLock(500, TimeUnit.MILLISECONDS)) {
                    System.out.println("Thread 1: 拿到 lockA");
                    Thread.sleep(100);
                    if (lockB.tryLock(500, TimeUnit.MILLISECONDS)) {
                        System.out.println("Thread 1: 拿到 lockB");
                        lockB.unlock();
                    } else {
                        System.out.println("Thread 1: 获取 lockB 超时，避免死锁");
                    }
                    lockA.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                if (lockB.tryLock(500, TimeUnit.MILLISECONDS)) {
                    System.out.println("Thread 2: 拿到 lockB");
                    Thread.sleep(100);
                    if (lockA.tryLock(500, TimeUnit.MILLISECONDS)) {
                        System.out.println("Thread 2: 拿到 lockA");
                        lockA.unlock();
                    } else {
                        System.out.println("Thread 2: 获取 lockA 超时，避免死锁");
                    }
                    lockB.unlock();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
    }
}
