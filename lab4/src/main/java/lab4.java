import java.util.concurrent.Semaphore;
import java.lang.Runnable;
import java.util.Random;

public class lab4 {

    private final int count = 5;

    private lab4() {

        Fork[] forks = new Fork[count];

        for(int i = 0; i < count; i++) {
            forks[i] = new Fork(1);
        }

        for(int i = 0; i < count; i++) {
            new Thread(new Philosopher(i, forks[(i + 1) % count], forks[i])).start();
        }

    }

    public static void main(String[] args) {
        new lab4();
    }
}



class Philosopher implements Runnable {

    private final float chanceToEat = 0.3f;
    private final float chanceToThink = 0.3f;

    private int number;

    private Fork leftFork;
    private Fork rightFork;

    private Random random;

    private STATES state;

    private enum STATES {
        THINKING,
        EATING;
    }

    public Philosopher(int number, Fork leftFork, Fork rightFork) {
        this.number = number;
        this.leftFork = leftFork;
        this.rightFork = rightFork;

        random = new Random(System.nanoTime());
        state = STATES.THINKING;
    }

    boolean isTimeToEat() {
        return random.nextFloat() < chanceToEat;
    }

    boolean isTimeToThink() {
        return random.nextFloat() < chanceToThink;
    }

    @Override
    public void run() {
        System.out.println("Филосов № " + number + " думает...");
        while(true) {
            try {
                switch(state) {
                    case THINKING:  if(isTimeToEat()) {
                        boolean wait = true;
                        int i = 0;
                        while(wait) {
                            synchronized(leftFork) {
                                synchronized(rightFork) {
                                    if(leftFork.availablePermits() > 0 && rightFork.availablePermits() > 0) {
                                        leftFork.acquire();
                                        System.out.println("Филосов № " + number + " забирает левую вилку...");
                                        rightFork.acquire();
                                        System.out.println("Филосов № " + number + " забирает правую вилку...");
                                        wait = false;
                                        System.out.println("Филосов № " + number + " ест спагетти");
                                        state = STATES.EATING;
                                    }
                                    else {
                                        if(i++ == 0) {
                                            System.out.println("Филосов № " + number + " думает...");
                                        }
                                        if(i > 100) {
                                            System.out.println("Конец трапезы и размышлений.\nDEADLOCK\tDEADLOCK\tDEADLOCK\tDEADLOCK\tDEADLOCK\t");
                                        }
                                        Thread.sleep(500);
                                    }
                                }
                            }
                        }
                    }
                        break;

                    case EATING:    if(isTimeToThink()) {
                        leftFork.release();
                        System.out.println("Филосов № " + number + " освобождает левую вилку...");
                        rightFork.release();
                        System.out.println("Филосов № " + number + " освобождает правую вилку...");
                        System.out.println("Филосов № " + number + " думает...");
                        state = STATES.THINKING;
                    }
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class Fork extends Semaphore {

    private static final long serialVersionUID = -9092936681187449008L;

    public Fork(int permits) {
        super(permits);
    }
}
