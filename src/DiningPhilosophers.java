import java.util.concurrent.Semaphore;

public class DiningPhilosophers {
    private Semaphore[] chopsticks = new Semaphore[5];
    private Semaphore maxDiners = new Semaphore(4); // 限制同时吃饭人数为 N-1

    public DiningPhilosophers() {
        for (int i = 0; i < 5; i++) {
            chopsticks[i] = new Semaphore(1); // 每根筷子相当于一个信号量
        }
    }

    public void wantsToEat(int philosopher,
                           Runnable pickLeft, Runnable pickRight,
                           Runnable eat,
                           Runnable putLeft, Runnable putRight) throws InterruptedException {
        maxDiners.acquire(); // 限制最多 4 个哲学家同时进餐，避免死锁
        int left = philosopher;
        int right = (philosopher + 1) % 5;

        chopsticks[left].acquire();
        chopsticks[right].acquire();

        pickLeft.run();
        pickRight.run();

        eat.run();

        putLeft.run();
        putRight.run();

        chopsticks[left].release();
        chopsticks[right].release();
        maxDiners.release();
    }
}

class Main {
    public static void main(String[] args) {
        DiningPhilosophers dp = new DiningPhilosophers();

        for (int i = 0; i < 5; i++) {
            int philosopher = i;
            new Thread(() -> {
                try {
                    while (true) { // 哲学家反复思考和吃饭
                        System.out.println("哲学家 " + philosopher + " 正在思考...");
                        Thread.sleep((long)(Math.random() * 2000)); // 模拟思考时间

                        dp.wantsToEat(
                                philosopher,
                                () -> System.out.println("哲学家 " + philosopher + " 拿起左筷子"),
                                () -> System.out.println("哲学家 " + philosopher + " 拿起右筷子"),
                                () -> {
                                    System.out.println("哲学家 " + philosopher + " 开始吃饭 🍜");
                                    try { Thread.sleep((long)(Math.random() * 2000)); } catch (InterruptedException e) {}
                                },
                                () -> System.out.println("哲学家 " + philosopher + " 放下左筷子"),
                                () -> System.out.println("哲学家 " + philosopher + " 放下右筷子")
                        );
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
