import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.*;
import java.util.concurrent.locks.*;

// ====== 严格读者优先（与课件伪代码等价）：ReentrantLock + 两个Condition ======
class RWReadersPriorityStrict {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition canRead  = lock.newCondition();   // OKtoread
    private final Condition canWrite = lock.newCondition();   // OKtowrite

    private int readers = 0;           // 正在读的读者数
    private boolean writing = false;   // 是否有写者在写
    private int waitingReaders = 0;    // 等待读者数（用于“读者优先”判断）

    public void startRead() throws InterruptedException {
        lock.lock();
        try {
            waitingReaders++;                // 有读者在等
            while (writing) {
                canRead.await();
            }
            waitingReaders--;
            readers++;
            // 链式放行：如果还有等待读者，继续叫下一个进来
            if (waitingReaders > 0) canRead.signal();
        } finally {
            lock.unlock();
        }
    }

    public void endRead() {
        lock.lock();
        try {
            readers--;
            if (readers == 0) {
                // 最后一个读者走了 → 允许写
                canWrite.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    public void startWrite() throws InterruptedException {
        lock.lock();
        try {
            while (writing || readers > 0) {
                canWrite.await();
            }
            writing = true;
        } finally {
            lock.unlock();
        }
    }

    public void endWrite() {
        lock.lock();
        try {
            writing = false;
            // 如果有等待读者，优先唤醒读者；否则唤醒写者
            if (waitingReaders > 0) {
                canRead.signal();
            } else {
                canWrite.signal();
            }
        } finally {
            lock.unlock();
        }
    }
}

public class RWReadersPriorityMonitor {

    // ====== 日志打印工具：含时间戳和线程名 ======
    static void log(String fmt, Object... args) {
        long ms = System.currentTimeMillis() % 100000;
        String who = Thread.currentThread().getName();
        System.out.printf("[%5d][%-12s] %s%n", ms, who, String.format(fmt, args));
    }

    public static void main(String[] args) throws Exception {
        final RWReadersPriorityStrict rw = new RWReadersPriorityStrict();

        final ExecutorService pool = Executors.newCachedThreadPool();
        final CountDownLatch allDone = new CountDownLatch(1);

        // 控制“读者洪峰”的开关
        final AtomicBoolean spawningReaders = new AtomicBoolean(true);

        // 统计
        final AtomicInteger readCount = new AtomicInteger();
        final AtomicInteger writeCount = new AtomicInteger();

        // ============== 1) 先启动一个写者（它会因读者而等待很久） ==============
        pool.submit(() -> {
            Thread.currentThread().setName("Writer-1");
            try {
                log("try startWrite()");
                rw.startWrite();
                log("BEGIN WRITE");
                Thread.sleep(300); // 模拟写入耗时
                log("END   WRITE");
                rw.endWrite();
                writeCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // 稍等片刻再放读者洪峰
        Thread.sleep(150);

        // ============== 2) 连续生成大量读者，模拟“源源不断的读请求” ==============
        Runnable readerTask = () -> {
            String name = "Reader-" + UUID.randomUUID().toString().substring(0, 4);
            Thread.currentThread().setName(name);
            try {
                rw.startRead();
                log("BEGIN READ");
                Thread.sleep(80 + ThreadLocalRandom.current().nextInt(70)); // 模拟读取耗时
                log("END   READ");
                rw.endRead();
                readCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        pool.submit(() -> {
            long endAt = System.currentTimeMillis() + 1600; // 1.6s 的读者洪峰
            while (System.currentTimeMillis() < endAt && spawningReaders.get()) {
                pool.submit(readerTask);
                try {
                    Thread.sleep(30); // 读者到达间隔短 → 强化“读者优先”的效果
                } catch (InterruptedException ignored) {}
            }
            spawningReaders.set(false);
            allDone.countDown();
        });

        // 等读者洪峰结束
        allDone.await();

        // ============== 3) 洪峰结束后，再来几个读者与一个写者，观察写者何时写 ==============
        pool.submit(() -> {
            Thread.currentThread().setName("Writer-2");
            try {
                log("try startWrite()");
                rw.startWrite();
                log("BEGIN WRITE");
                Thread.sleep(200);
                log("END   WRITE");
                rw.endWrite();
                writeCount.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        for (int i = 0; i < 5; i++) {
            pool.submit(readerTask);
            Thread.sleep(60);
        }

        // 等待所有任务基本完成
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);

        log("SUMMARY: reads=%d, writes=%d", readCount.get(), writeCount.get());
        System.out.println();
        System.out.println("观察点：");
        System.out.println("1) Writer-1 很早到达，但要等大量 Reader 完成后才写（读者优先）。");
        System.out.println("2) 写者等待期间，只要没有写者正在写，新读者仍可进入。");
        System.out.println("3) 读者清零 (readers==0) 才会给写者机会。读者持续涌入时，写者可能饥饿。");
    }
}
