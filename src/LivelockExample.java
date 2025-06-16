public class LivelockExample {
    static class Spoon {
        private Diner owner;

        public Spoon(Diner d) {
            owner = d;
        }

        public Diner getOwner() {
            return owner;
        }

        public synchronized void setOwner(Diner d) {
            owner = d;
        }

        public synchronized void use() {
            System.out.println(owner.name + " 吃完了饭！");
        }
    }

    static class Diner {
        private final String name;
        private boolean isHungry = true;

        public Diner(String name) {
            this.name = name;
        }

        public void eatWith(Spoon spoon, Diner partner) {
            while (isHungry) {
                if (spoon.getOwner() != this) {
                    try { Thread.sleep(1); } catch (InterruptedException e) {}
                    continue;
                }

                if (partner.isHungry) {
                    System.out.println(name + "：你先吃吧 " + partner.name);
                    spoon.setOwner(partner);
                    continue;
                }

                spoon.use();
                isHungry = false;
                System.out.println(name + "：吃完了！");
                spoon.setOwner(partner);
            }
        }
    }

    public static void main(String[] args) {
        final Diner alice = new Diner("Alice");
        final Diner bob = new Diner("Bob");
        final Spoon spoon = new Spoon(alice);

        Thread t1 = new Thread(() -> alice.eatWith(spoon, bob));
        Thread t2 = new Thread(() -> bob.eatWith(spoon, alice));

        t1.start();
        t2.start();
    }
}
