import java.util.concurrent.atomic.AtomicInteger;

public class ProducerConsumerMonitor {

    // ===== Monitor：有界缓冲区 =====
    static class BoundedBuffer<T> {
        private final Object[] buffer;
        private int count = 0, in = 0, out = 0; // 共享状态：元素数、写指针、读指针

        public BoundedBuffer(int capacity) {
            this.buffer = new Object[capacity];
        }

        // 生产者插入
        public synchronized void insert(T item) throws InterruptedException {
            while (count == buffer.length) { // 缓冲区满 → 等
                wait();
            }
            buffer[in] = item;
            in = (in + 1) % buffer.length;
            count++;
            notifyAll(); // 通知消费者：可能有新元素了
        }

        // 消费者取出
        @SuppressWarnings("unchecked")
        public synchronized T remove() throws InterruptedException {
            while (count == 0) { // 缓冲区空 → 等
                wait();
            }
            T item = (T) buffer[out];
            buffer[out] = null; // 非必须，但便于观察
            out = (out + 1) % buffer.length;
            count--;
            notifyAll(); // 通知生产者：可能有空位了
            return item;
        }

        public synchronized int size() {
            return count;
        }
    }

    // ===== 生产者 =====
    static class Producer extends Thread {
        private final BoundedBuffer<Integer> buffer;
        private final int totalToProduce;
        private static final AtomicInteger globalSeq = new AtomicInteger(1);

        public Producer(String name, BoundedBuffer<Integer> buffer, int totalToProduce) {
            super(name);
            this.buffer = buffer;
            this.totalToProduce = totalToProduce;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < totalToProduce; i++) {
                    int item = globalSeq.getAndIncrement();
                    buffer.insert(item);
                    System.out.printf("[%s] produced: %d  (buffer size=%d)%n",
                            getName(), item, buffer.size());
                    // 模拟不规则生产速度
                    Thread.sleep(50 + (int)(Math.random() * 120));
                }
            } catch (InterruptedException e) {
                System.out.printf("[%s] interrupted%n", getName());
                Thread.currentThread().interrupt();
            }
        }
    }

    // ===== 消费者 =====
    static class Consumer extends Thread {
        private final BoundedBuffer<Integer> buffer;
        private final int totalToConsume;

        public Consumer(String name, BoundedBuffer<Integer> buffer, int totalToConsume) {
            super(name);
            this.buffer = buffer;
            this.totalToConsume = totalToConsume;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < totalToConsume; i++) {
                    Integer item = buffer.remove();
                    System.out.printf("                          [%s] consumed: %d  (buffer size=%d)%n",
                            getName(), item, buffer.size());
                    // 模拟不规则消费速度
                    Thread.sleep(80 + (int)(Math.random() * 150));
                }
            } catch (InterruptedException e) {
                System.out.printf("[%s] interrupted%n", getName());
                Thread.currentThread().interrupt();
            }
        }
    }

    // ===== 主程序：2 个生产者 + 2 个消费者 =====
    public static void main(String[] args) throws InterruptedException {
        int capacity = 5;          // 缓冲区容量
        int perProducer = 10;      // 每个生产者生产 10 个
        int perConsumer = 10;      // 每个消费者消费 10 个

        BoundedBuffer<Integer> buffer = new BoundedBuffer<>(capacity);

        Thread p1 = new Producer("Producer-1", buffer, perProducer);
        Thread p2 = new Producer("Producer-2", buffer, perProducer);
        Thread c1 = new Consumer("Consumer-1", buffer, perConsumer);
        Thread c2 = new Consumer("Consumer-2", buffer, perConsumer);

        long start = System.currentTimeMillis();

        p1.start(); p2.start(); c1.start(); c2.start();

        // 等待全部结束
        p1.join(); p2.join(); c1.join(); c2.join();

        long elapsed = System.currentTimeMillis() - start;
        System.out.println("\nAll done. Final buffer size = " + buffer.size()
                + ", elapsed = " + elapsed + " ms");
    }
}
