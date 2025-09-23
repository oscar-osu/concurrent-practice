public class BinarySemaphore {
    // 对应课件里的 busy 布尔量：false=空闲，true=占用
    private boolean busy = false;

    // acquire()：如果被占用就等待；否则占用并返回
    public synchronized void acquire() throws InterruptedException {
        // 课件在 Linux 上强调用 while 抵御伪唤醒与竞态
        // （而不是 if）——见“Text book uses if ... On Linux, always use while.”
        while (busy) {
            wait();
        }
        busy = true;
    }

    // release()：释放占用并唤醒等待者
    public synchronized void release() {
        busy = false;
        notifyAll(); // 唤醒可能在等待 acquire() 的线程
    }
}
