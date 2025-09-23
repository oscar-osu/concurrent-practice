public class BankTest {
    public static void main(String[] args) throws InterruptedException {
        BankAccount account = new BankAccount();

        // 存钱线程
        Thread depositor = new Thread(() -> {
            try {
                Thread.sleep(1000); // 模拟延迟
                account.deposit(100);
                System.out.println("存入 100");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        // 取钱线程
        Thread withdrawer = new Thread(() -> {
            try {
                account.withdraw(50);
                System.out.println("取出 50");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        depositor.start();
        withdrawer.start();

        depositor.join();
        withdrawer.join();

        System.out.println("最终余额: " + account.getBalance());
    }
}
