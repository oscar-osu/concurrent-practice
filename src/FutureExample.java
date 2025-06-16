import java.util.concurrent.*;

public class FutureExample {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        // 提交一个带返回值的任务（Callable）
        Future<Integer> future = executor.submit(() -> {
            Thread.sleep(2000); // 模拟计算
            return 42;
        });

        System.out.println("任务已提交，去干别的事...");

        // 阻塞等待结果（如果任务还没完成，就会阻塞）
        Integer result = future.get();
        System.out.println("任务结果是：" + result);

        executor.shutdown();
    }
}
