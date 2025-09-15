import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ProducerConsumerCV {
    private static final int BUFFER_SIZE = 5;
    private static final int[] buffer = new int[BUFFER_SIZE];
    private static int count = 0, head = 0, tail = 0;

    private static final Lock lock = new ReentrantLock();
    private static final Condition notFull = lock.newCondition();  // 缓冲区不满的条件
    private static final Condition notEmpty = lock.newCondition(); // 缓冲区不空的条件

    static class Producer implements Runnable {
        private final int id;
        private final int itemsToProduce;

        public Producer(int id, int itemsToProduce) {
            this.id = id;
            this.itemsToProduce = itemsToProduce;
        }

        public void run() {
            try {
                for (int i = 0; i < itemsToProduce; i++) {
                    int item = (int)(Math.random() * 100);

                    lock.lock();
                    try {
                        // 使用while循环检查条件（课件强调的重点）
                        while (count == BUFFER_SIZE) {
                            System.out.printf("Producer %d waiting (buffer full, count: %d)\n", id, count);
                            notFull.await(); // 等待缓冲区不满
                        }

                        // 生产物品
                        buffer[head] = item;
                        head = (head + 1) % BUFFER_SIZE;
                        count++;
                        System.out.printf("Producer %d produced: %d (count: %d)\n", id, item, count);

                        notEmpty.signalAll(); // 通知消费者（使用broadcast，课件推荐）
                    } finally {
                        lock.unlock();
                    }

                    Thread.sleep((int)(Math.random() * 200));
                }
                System.out.printf("Producer %d finished production\n", id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    static class Consumer implements Runnable {
        private final int id;
        private final int itemsToConsume;

        public Consumer(int id, int itemsToConsume) {
            this.id = id;
            this.itemsToConsume = itemsToConsume;
        }

        public void run() {
            try {
                for (int i = 0; i < itemsToConsume; i++) {
                    lock.lock();
                    try {
                        // 使用while循环检查条件
                        while (count == 0) {
                            System.out.printf("Consumer %d waiting (buffer empty, count: %d)\n", id, count);
                            notEmpty.await(); // 等待缓冲区不空
                        }

                        // 消费物品
                        int item = buffer[tail];
                        tail = (tail + 1) % BUFFER_SIZE;
                        count--;
                        System.out.printf("Consumer %d consumed: %d (count: %d)\n", id, item, count);

                        notFull.signalAll(); // 通知生产者（使用broadcast）
                    } finally {
                        lock.unlock();
                    }

                    Thread.sleep((int)(Math.random() * 300));
                }
                System.out.printf("Consumer %d finished consumption\n", id);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final int PRODUCER_COUNT = 3;
        final int CONSUMER_COUNT = 2;
        final int ITEMS_PER_PRODUCER = 10;
        final int ITEMS_PER_CONSUMER = 15;

        Thread[] producers = new Thread[PRODUCER_COUNT];
        Thread[] consumers = new Thread[CONSUMER_COUNT];

        System.out.println("Starting Producer-Consumer with Condition Variables");
        System.out.println("Buffer size: " + BUFFER_SIZE);
        System.out.println("Producers: " + PRODUCER_COUNT + ", Consumers: " + CONSUMER_COUNT);
        System.out.println("----------------------------------------");

        // 创建并启动生产者线程
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            producers[i] = new Thread(new Producer(i, ITEMS_PER_PRODUCER), "Producer-" + i);
            producers[i].start();
        }

        // 创建并启动消费者线程
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumers[i] = new Thread(new Consumer(i, ITEMS_PER_CONSUMER), "Consumer-" + i);
            consumers[i].start();
        }

        // 等待所有生产者完成
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            producers[i].join();
        }

        // 等待所有消费者完成
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumers[i].join();
        }

        System.out.println("----------------------------------------");
        System.out.println("All producers and consumers have finished.");
        System.out.println("Final buffer count: " + count);
    }
}