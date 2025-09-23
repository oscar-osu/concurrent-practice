public class BinarySemaphoreDemo {
    public static void main(String[] args) {
        BinarySemaphore sem = new BinarySemaphore();

        Runnable criticalTask = () -> {
            String name = Thread.currentThread().getName();
            try {
                sem.acquire();
                System.out.println(name + " ENTER");
                Thread.sleep(200); // 模拟临界区工作
                System.out.println(name + " EXIT");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                sem.release();
            }
        };

        // 启动多个线程竞争同一把“二元信号量”
        for (int i = 0; i < 5; i++) {
            new Thread(criticalTask, "T" + i).start();
        }
    }
}