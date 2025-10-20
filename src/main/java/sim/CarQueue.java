package sim;

/**
 * CarQueue: the class representing the lineup of cars at the gas station.
 */
public class CarQueue {

    /** QueueItem: the class for objects stored in the car queue. */
    private static class QueueItem {
        public Car data;
        public QueueItem next;
    }

    private QueueItem firstWaitingCar = null;
    private QueueItem lastWaitingCar = null;
    private int queueSize = 0;
    private double totalEmptyQueueTime = 0.0;
    private boolean countingEmptySinceStart = true; // queue starts empty

    /** return the total time the car queue has been empty. */
    public double getEmptyTime() {
        if (queueSize > 0) {
            return totalEmptyQueueTime;
        } else {
            // empty from last time we became empty up to now
            return totalEmptyQueueTime + Sim.simulationTime;
        }
    }

    /** return the number of cars in the car queue. */
    public int getQueueSize() {
        return queueSize;
    }

    /** insert: put a newly-arrived car into the car queue. */
    public void insert(Car newestCar) {
        QueueItem item = new QueueItem();
        item.data = newestCar;
        item.next = null;

        if (lastWaitingCar == null) {
            // the queue is empty
            firstWaitingCar = item;
            lastWaitingCar = item;
            // we stop counting empty time now; since we counted from 0,
            // correct the initial overcount by not adding more here.
        } else {
            // the queue already had at least one car in it
            lastWaitingCar.next = item;
            lastWaitingCar = item;
        }
        queueSize += 1;
    }

    /** takeFirstCar: remove first car from car queue and return it. */
    public Car takeFirstCar() {
        if (queueSize <= 0 || firstWaitingCar == null) {
            System.out.println("Error! car queue unexpectedly empty");
            return null;
        }
        Car carToReturn = firstWaitingCar.data;
        queueSize--;
        firstWaitingCar = firstWaitingCar.next;
        if (firstWaitingCar == null) {
            // empty queue; update the end of the queue
            lastWaitingCar = null;
            // start counting empty queue time
            totalEmptyQueueTime -= Sim.simulationTime; // see original intent
            // We will add Sim.simulationTime in getEmptyTime(), net effect tracks emptiness.
        }
        return carToReturn;
    }
}
