public class BankConcurrentTest {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();

        // 启动 3 个存钱线程
        for (int i = 0; i < 3; i++) {
            int amount = (i + 1) * 50;
            new Thread(() -> {
                try {
                    Thread.sleep((long)(Math.random() * 2000));
                    account.deposit(amount);
                    System.out.println(Thread.currentThread().getName() + " 存入 " + amount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Depositor-" + i).start();
        }

        // 启动 3 个取钱线程
        for (int i = 0; i < 3; i++) {
            int amount = 40;
            new Thread(() -> {
                try {
                    account.withdraw(amount);
                    System.out.println(Thread.currentThread().getName() + " 取出 " + amount);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }, "Withdrawer-" + i).start();
        }

        // 等一会儿再看余额
        Thread.sleep(5000);
        System.out.println("最终余额: " + account.getBalance());
    }
}
