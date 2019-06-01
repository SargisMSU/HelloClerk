import clerk.model.Departament;
import clerk.model.Worker;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;

public class Test {
    public static void main(String[] args) throws InterruptedException {
        Worker worker = new Worker("name", "surname", "mail", new Departament("dep"), Worker.Position.DIRECTOR);
        Worker worker1 = new Worker("name", "surname", "mail", new Departament("dep"), Worker.Position.DIRECTOR);
        Worker worker2 = new Worker("name", "surname", "mail", new Departament("dep"), Worker.Position.DIRECTOR);
        Worker worker3 = new Worker("name", "surname", "mail", new Departament("dep"), Worker.Position.DIRECTOR);
        ArrayList<Worker> workers = new ArrayList<>();
        workers.add(worker);
        workers.add(worker1);
        workers.add(worker2);
        workers.add(worker3);

        long count = workers.stream().filter(work -> work.getPosition().getCode() < 11).count();
        //System.out.println("count = " + count);

        Semaphore semaphore = new Semaphore(1);
        semaphore.acquire();
        semaphore.release();
        semaphore.release();
    }
}
