import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ThreadPoolExample {
    public static void main(String[] args) {
        // 创建一个固定大小为 4 的线程池
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // 提交 10 个任务给线程池执行
        for (int i = 1; i <= 10; i++) {
            int taskId = i; // 注意：必须是 final 或 effectively final
            executor.submit(() -> {
                System.out.println("任务 " + taskId + " 由线程 " + Thread.currentThread().getName() + " 执行");
                try {
                    Thread.sleep(1000); // 模拟任务耗时
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }

        // 关闭线程池（不再接收新任务）
        executor.shutdown();

        try {
            // 等待线程池中的所有任务执行完毕（最多等待 60 秒）
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // 强制关闭
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        System.out.println("所有任务执行完毕，线程池关闭");
    }
}
