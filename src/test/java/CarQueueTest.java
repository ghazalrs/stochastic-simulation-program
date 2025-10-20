import org.junit.jupiter.api.Test;
import sim.Sim;
import sim.Car;
import sim.CarQueue;

import static org.junit.jupiter.api.Assertions.*;

public class CarQueueTest {

    @Test
    void carsComeOutInSameOrder() {
        Sim.litreStream = new java.util.Random(1);
        Sim.arrivalStream = new java.util.Random(2);
        Sim.balkingStream = new java.util.Random(3);
        Sim.serviceStream = new java.util.Random(4);

        CarQueue queue = new CarQueue();
        Car car1 = new Car();
        Car car2 = new Car();

        queue.insert(car1);
        queue.insert(car2);

        assertSame(car1, queue.takeFirstCar());
        assertSame(car2, queue.takeFirstCar());
    }
}

