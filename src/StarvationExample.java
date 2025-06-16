public class StarvationExample {
    public static void main(String[] args) {
        Object lock = new Object();

        Runnable highPriorityTask = () -> {
            while (true) {
                synchronized (lock) {
                    System.out.println("🔥 高优先级线程正在运行");
                }
            }
        };

        Runnable lowPriorityTask = () -> {
            while (true) {
                synchronized (lock) {
                    System.out.println("😢 低优先级线程尝试运行");
                }
            }
        };

        Thread t1 = new Thread(highPriorityTask);
        Thread t2 = new Thread(lowPriorityTask);

        t1.setPriority(Thread.MAX_PRIORITY); // 优先级 10
        t2.setPriority(Thread.MIN_PRIORITY); // 优先级 1

        t1.start();
        t2.start();
    }
}
