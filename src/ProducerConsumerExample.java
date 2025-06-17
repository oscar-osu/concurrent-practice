import java.util.concurrent.*;
import java.util.Random;

public class ProducerConsumerExample {
    private static final int QUEUE_CAPACITY = 5;
    private static final int PRODUCER_COUNT = 3;
    private static final int CONSUMER_COUNT = 2;
    private static final int ITEM_PER_PRODUCER = 10;

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
        ExecutorService executor = Executors.newFixedThreadPool(PRODUCER_COUNT + CONSUMER_COUNT);

        CountDownLatch latch = new CountDownLatch(PRODUCER_COUNT + CONSUMER_COUNT);

        // Start producers
        for (int i = 0; i < PRODUCER_COUNT; i++) {
            int producerId = i;
            executor.submit(() -> {
                Random rand = new Random();
                for (int j = 0; j < ITEM_PER_PRODUCER; j++) {
                    int item = rand.nextInt(100);
                    try {
                        queue.put(item); // blocks if queue is full
                        System.out.printf("Producer %d produced: %d\n", producerId, item);
                        Thread.sleep(rand.nextInt(200)); // simulate time to produce
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                latch.countDown();
            });
        }

        // Start consumers
        for (int i = 0; i < CONSUMER_COUNT; i++) {
            int consumerId = i;
            executor.submit(() -> {
                try {
                    int consumed = 0;
                    while (true) {
                        Integer item = queue.poll(1, TimeUnit.SECONDS);
                        if (item == null) {
                            break; // exit if no item for 1 second
                        }
                        System.out.printf("Consumer %d consumed: %d\n", consumerId, item);
                        consumed++;
                        Thread.sleep(new Random().nextInt(300)); // simulate time to process
                    }
                    System.out.printf("Consumer %d finished, total consumed: %d\n", consumerId, consumed);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // wait for all producers and consumers to finish
        executor.shutdown();
        System.out.println("All tasks finished.");
    }
}
