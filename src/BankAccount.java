class BankAccount { // 🔹 管程 Monitor
    private int balance = 0;   // 🔹 封装的数据（共享变量）

    // 存钱
    public synchronized void deposit(int amount) {
        balance += amount;
        notifyAll();           // 🔹 条件变量操作
    }

    // 取钱
    public synchronized void withdraw(int amount) throws InterruptedException {
        while (balance < amount) {
            wait();            // 🔹 条件变量操作
        }
        balance -= amount;
    }

    public synchronized int getBalance() {
        return balance;
    }
}
