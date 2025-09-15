import java.util.concurrent.Semaphore;

public class ProducerConsumerSemaphore {
    private static final int BUFFER_SIZE = 5;
    private static final int[] buffer = new int[BUFFER_SIZE];
    private static int count = 0, head = 0, tail = 0;

    private static final Semaphore mutex = new Semaphore(1);      // 互斥信号量
    private static final Semaphore empty = new Semaphore(BUFFER_SIZE); // 空槽信号量
    private static final Semaphore full = new Semaphore(0);       // 满槽信号量

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

                    empty.acquire();    // P(empty) - 等待空槽
                    mutex.acquire();    // P(mutex) - 进入临界区

                    // 生产物品
                    buffer[head] = item;
                    head = (head + 1) % BUFFER_SIZE;
                    count++;
                    System.out.printf("Producer %d produced: %d (count: %d)\n", id, item, count);

                    mutex.release();    // V(mutex) - 离开临界区
                    full.release();     // V(full) - 增加满槽计数

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
                    full.acquire();     // P(full) - 等待有物品
                    mutex.acquire();    // P(mutex) - 进入临界区

                    // 消费物品
                    int item = buffer[tail];
                    tail = (tail + 1) % BUFFER_SIZE;
                    count--;
                    System.out.printf("Consumer %d consumed: %d (count: %d)\n", id, item, count);

                    mutex.release();    // V(mutex) - 离开临界区
                    empty.release();    // V(empty) - 增加空槽计数

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
        final int ITEMS_PER_CONSUMER = 15; // 3生产者×10=30，2消费者×15=30

        Thread[] producers = new Thread[PRODUCER_COUNT];
        Thread[] consumers = new Thread[CONSUMER_COUNT];

        // 创建并启动生产者线程
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            producers[i] = new Thread(new Producer(i, ITEMS_PER_PRODUCER));
            producers[i].start();
        }

        // 创建并启动消费者线程
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            consumers[i] = new Thread(new Consumer(i, ITEMS_PER_CONSUMER));
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

        System.out.println("All producers and consumers have finished.");
        System.out.println("Final buffer count: " + count);
    }
}