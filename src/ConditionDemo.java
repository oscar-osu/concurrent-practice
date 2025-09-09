import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ConditionDemo {

    // 有界缓冲区
    static class BoundedBuffer<T> {
        private final Queue<T> queue = new ArrayDeque<>();
        private final int capacity;

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition notEmpty = lock.newCondition(); // 队列非空条件
        private final Condition notFull = lock.newCondition();  // 队列未满条件

        public BoundedBuffer(int capacity) {
            if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
            this.capacity = capacity;
        }

        public void put(T item) throws InterruptedException {
            lock.lock();
            try {
                // 队列满了，等待 notFull
                while (queue.size() == capacity) {
                    notFull.await(); // 释放锁并挂起，直到被signal/signalAll唤醒再重新竞争锁
                }
                queue.add(item);
                // 放入一个元素后，队列肯定非空了，唤醒一个在 notEmpty 等待的线程
                notEmpty.signal();
            } finally {
                lock.unlock();
            }
        }

        public T take() throws InterruptedException {
            lock.lock();
            try {
                // 队列空，等待 notEmpty
                while (queue.isEmpty()) {
                    notEmpty.await();
                }
                T item = queue.remove();
                // 取出一个元素后，队列肯定未满了，唤醒一个在 notFull 等待的线程
                notFull.signal();
                return item;
            } finally {
                lock.unlock();
            }
        }

        // 非必须：查询当前大小（仅示例，不保证强一致，仅用于日志）
        public int size() {
            lock.lock();
            try {
                return queue.size();
            } finally {
                lock.unlock();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(5);
        // 生产者
        Runnable producer = () -> {
            try {
                for (int i = 1; i <= 20; i++) {
                    buffer.put(i);
                    System.out.printf("[%s] produced %d (size=%d)%n",
                            Thread.currentThread().getName(), i, buffer.size());
                    // 模拟生产开销
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // 消费者
        Runnable consumer = () -> {
            try {
                for (int i = 1; i <= 10; i++) {
                    Integer x = buffer.take();
                    System.out.printf("  [%s] consumed %d (size=%d)%n",
                            Thread.currentThread().getName(), x, buffer.size());
                    // 模拟处理开销
                    Thread.sleep(120);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread p1 = new Thread(producer, "P-1");
        Thread p2 = new Thread(producer, "P-2");
        Thread c1 = new Thread(consumer, "C-1");
        Thread c2 = new Thread(consumer, "C-2");

        p1.start(); p2.start(); c1.start(); c2.start();

        // 等待线程结束
        p1.join(); p2.join(); c1.join(); c2.join();

        System.out.println("Done.");
    }
}
