import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class ReaderWritersProblemCV {
    private int readerCount = 0;        // 当前读者数量
    private int writerCount = 0;        // 当前写者数量（等待或正在写）
    private int activeWriters = 0;      // 正在写的写者数量

    private final Lock lock = new ReentrantLock();
    private final Condition canRead = lock.newCondition();    // 可以读的条件
    private final Condition canWrite = lock.newCondition();   // 可以写的条件

    // 读者开始读
    public void startRead(int readerId) throws InterruptedException {
        lock.lock();
        try {
            // 使用while循环检查条件（课件强调的重点）
            while (writerCount > 0 || activeWriters > 0) {
                System.out.println("Reader " + readerId + " 等待（有写者活跃或等待）");
                canRead.await(); // 等待可以读的条件
            }

            readerCount++;
            System.out.println("Reader " + readerId + " 开始读取 📖 (读者数: " + readerCount + ")");
        } finally {
            lock.unlock();
        }
    }

    // 读者结束读
    public void endRead(int readerId) throws InterruptedException {
        lock.lock();
        try {
            readerCount--;
            System.out.println("Reader " + readerId + " 结束读取 ✅ (读者数: " + readerCount + ")");

            // 使用broadcast通知所有等待者（课件推荐的做法）
            if (readerCount == 0) {
                canWrite.signalAll(); // 通知所有等待的写者
            }
        } finally {
            lock.unlock();
        }
    }

    // 写者开始写
    public void startWrite(int writerId) throws InterruptedException {
        lock.lock();
        try {
            writerCount++; // 增加等待的写者计数

            // 使用while循环检查条件
            while (readerCount > 0 || activeWriters > 0) {
                System.out.println("Writer " + writerId + " 等待（有读者或其他写者）");
                canWrite.await(); // 等待可以写的条件
            }

            activeWriters++;
            writerCount--;
            System.out.println("Writer " + writerId + " 开始写入 ✍️");
        } finally {
            lock.unlock();
        }
    }

    // 写者结束写
    public void endWrite(int writerId) throws InterruptedException {
        lock.lock();
        try {
            activeWriters--;
            System.out.println("Writer " + writerId + " 完成写入 ✅");

            // 使用broadcast通知所有等待者
            if (writerCount > 0) {
                canWrite.signalAll(); // 优先通知其他写者（写者优先）
            } else {
                canRead.signalAll();  // 没有写者等待，通知所有读者
            }
        } finally {
            lock.unlock();
        }
    }

    // 写操作（包含开始和结束）
    public void write(int writerId) throws InterruptedException {
        startWrite(writerId);
        try {
            Thread.sleep((long) (Math.random() * 2000)); // 模拟写操作
        } finally {
            endWrite(writerId);
        }
    }

    public static void main(String[] args) {
        ReaderWritersProblemCV rw = new ReaderWritersProblemCV();

        System.out.println("读者-写者问题（条件变量实现）");
        System.out.println("========================================");

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
                    Thread.currentThread().interrupt();
                }
            }, "Reader-" + readerId).start();
        }

        // 启动多个写者线程
        for (int i = 1; i <= 2; i++) {
            int writerId = i;
            new Thread(() -> {
                try {
                    while (true) {
                        Thread.sleep((long) (Math.random() * 4000)); // 模拟思考
                        rw.write(writerId);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Writer-" + writerId).start();
        }
    }
}