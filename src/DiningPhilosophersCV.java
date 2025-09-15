import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class DiningPhilosophersCV {
    private enum State { THINKING, HUNGRY, EATING }

    private State[] states = new State[5];
    private Lock lock = new ReentrantLock();
    private Condition[] conditions = new Condition[5];

    public DiningPhilosophersCV() {
        for (int i = 0; i < 5; i++) {
            states[i] = State.THINKING;
            conditions[i] = lock.newCondition();
        }
    }

    public void wantsToEat(int philosopher,
                           Runnable pickLeft, Runnable pickRight,
                           Runnable eat,
                           Runnable putLeft, Runnable putRight) throws InterruptedException {

        lock.lock();
        try {
            // 设置状态为饥饿
            states[philosopher] = State.HUNGRY;

            // 尝试获取筷子，如果不能就等待
            while (!canEat(philosopher)) {
                conditions[philosopher].await();
            }

            // 可以吃饭了，设置状态
            states[philosopher] = State.EATING;
        } finally {
            lock.unlock();
        }

        // 执行拿筷子和吃饭动作（在锁外执行）
        pickLeft.run();
        pickRight.run();

        eat.run();

        putLeft.run();
        putRight.run();

        // 吃完饭，更新状态并通知邻居
        lock.lock();
        try {
            states[philosopher] = State.THINKING;

            // 通知左右邻居检查是否可以吃饭
            checkNeighbor((philosopher + 4) % 5); // 左邻居
            checkNeighbor((philosopher + 1) % 5); // 右邻居
        } finally {
            lock.unlock();
        }
    }

    private boolean canEat(int philosopher) {
        int left = (philosopher + 4) % 5;
        int right = (philosopher + 1) % 5;

        // 可以吃饭的条件：自己饥饿，且左右邻居都不在吃饭
        return states[philosopher] == State.HUNGRY &&
                states[left] != State.EATING &&
                states[right] != State.EATING;
    }

    private void checkNeighbor(int philosopher) {
        lock.lock();
        try {
            if (canEat(philosopher)) {
                conditions[philosopher].signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        DiningPhilosophersCV dp = new DiningPhilosophersCV();

        // 创建5个哲学家线程
        for (int i = 0; i < 5; i++) {
            final int philosopher = i;
            new Thread(() -> {
                try {
                    while (true) {
                        // 思考
                        System.out.println("哲学家 " + philosopher + " 正在思考...");
                        Thread.sleep((long)(Math.random() * 1000));

                        // 尝试吃饭
                        dp.wantsToEat(
                                philosopher,
                                () -> System.out.println("哲学家 " + philosopher + " 拿起左筷子"),
                                () -> System.out.println("哲学家 " + philosopher + " 拿起右筷子"),
                                () -> {
                                    System.out.println("哲学家 " + philosopher + " 开始吃饭 🍜");
                                    try {
                                        Thread.sleep((long)(Math.random() * 1000));
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                },
                                () -> System.out.println("哲学家 " + philosopher + " 放下左筷子"),
                                () -> System.out.println("哲学家 " + philosopher + " 放下右筷子")
                        );
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("哲学家 " + philosopher + " 被中断");
                }
            }).start();
        }

        // 让程序运行一段时间后自动结束（可选）
        try {
            Thread.sleep(30000); // 运行30秒
            System.out.println("程序运行结束");
            System.exit(0);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}