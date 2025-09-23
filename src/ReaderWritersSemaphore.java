import java.util.concurrent.Semaphore;

public class ReaderWritersSemaphore {
    private int readerCount = 0; // 当前读者数量
    private Semaphore mutex = new Semaphore(1); // 控制 readerCount 的互斥
    private Semaphore writeLock = new Semaphore(1); // 控制写操作

    // 读者开始读
    public void startRead(int readerId) throws InterruptedException {
        mutex.acquire();
        readerCount++;
        if (readerCount == 1) {
            writeLock.acquire(); // 第一个读者锁写，避免写者进入
        }
        mutex.release();
        System.out.println("Reader " + readerId + " 开始读取 📖");
    }

    // 读者结束读
    public void endRead(int readerId) throws InterruptedException {
        mutex.acquire();
        readerCount--;
        if (readerCount == 0) {
            writeLock.release(); // 最后一个读者释放写锁
        }
        mutex.release();
        System.out.println("Reader " + readerId + " 结束读取 ✅");
    }

    // 写者写
    public void write(int writerId) throws InterruptedException {
        writeLock.acquire();
        System.out.println("Writer " + writerId + " 开始写入 ✍️");
        Thread.sleep((long) (Math.random() * 2000)); // 模拟写操作
        System.out.println("Writer " + writerId + " 完成写入 ✅");
        writeLock.release();
    }

    public static void main(String[] args) {
        ReaderWritersSemaphore rw = new ReaderWritersSemaphore();
        // 启动多个读者线程
        for (int i = 1; i <= 3; i++) {
            int readerId = i;
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep((long) (Math.random() * 3000)); // 模拟思考
                        rw.startRead(readerId);
                        Thread.sleep((long) (Math.random() * 1500)); // 模拟读操作
                        rw.endRead(readerId);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }

        // 启动两个写者线程
        for (int i = 1; i <= 2; i++) {
            int writerId = i;
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep((long) (Math.random() * 4000)); // 模拟思考
                        rw.write(writerId);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
