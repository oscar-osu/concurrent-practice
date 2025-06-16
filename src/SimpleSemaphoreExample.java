import java.util.concurrent.Semaphore;

public class SimpleSemaphoreExample {
    public static void main(String[] args) {
        // 允许最多 3 个线程同时进入
        Semaphore semaphore = new Semaphore(3);

        for (int i = 1; i <= 10; i++) {
            int taskId = i;
            new Thread(() -> {
                try {
                    semaphore.acquire(); // 请求一个许可（没拿到就阻塞）
                    System.out.println("任务 " + taskId + " 开始执行，线程：" + Thread.currentThread().getName());
                    Thread.sleep(1000); // 模拟执行任务
                    System.out.println("任务 " + taskId + " 执行完毕");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    semaphore.release(); // 释放许可
                }
            }).start();
        }
    }
}
