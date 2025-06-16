public class DeadlockExample {
    private static final Object lockA = new Object();
    private static final Object lockB = new Object();

    public static void main(String[] args) {
        Runnable task = () -> {
            synchronized (lockA) {
                System.out.println(Thread.currentThread().getName() + "：持有 lockA，准备获取 lockB...");
                try { Thread.sleep(100); } catch (InterruptedException e) {}
                synchronized (lockB) {
                    System.out.println(Thread.currentThread().getName() + "：拿到 lockB！");
                }
            }
        };

        Thread t1 = new Thread(task, "Thread 1");
        Thread t2 = new Thread(task, "Thread 2");

        t1.start();
        t2.start();
    }
}
