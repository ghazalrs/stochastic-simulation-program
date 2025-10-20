package sim;

/**
 * Arrival: the class representing arrival events.
 */
class Arrival extends Event {

    public Arrival(double time) {
        super(time);
    }

    /**
     * doesCarBalk: decide whether a car should balk.
     * The probability that a car leaves without buying gas (i.e., balks) grows larger as the
     * queue length gets larger, and grows smaller when the car requires a greater number of litres of gas.
     * (1) there is no balking if the queue length is zero, and
     * (2) otherwise, the probability of NOT balking is (40 + litres) / (25 * (3 + queueLength))
     */
    private boolean doesCarBalk(double litres, int queueLength) {
        if (queueLength == 0) return false;
        double pNotBalk = (Sim.balkA + litres) / (Sim.balkB * (Sim.balkC + queueLength));
        if (pNotBalk < 0.0) pNotBalk = 0.0;
        if (pNotBalk > 1.0) pNotBalk = 1.0;
        return Sim.balkingStream.nextDouble() > pNotBalk;
    }

    /** interarrivalTime: the time until the next arrival, from an exponential distribution. */
    private double interarrivalTime() {
        double u = Math.max(1e-12, Sim.arrivalStream.nextDouble());
        return -Sim.meaninterarrivalTime * Math.log(u);
    }

    /** arrival event routine. */
    public void makeItHappen() {
        // Create and initialize a new auto record.
        Car arrivingCar = new Car();
        Sim.stats.countArrival();
        final double litres = arrivingCar.getLitresNeeded();

        if (doesCarBalk(litres, Sim.carQueue.getQueueSize())) {
            Sim.stats.accumBalk(litres);
        } else {
            arrivingCar.setArrivalTime(Sim.simulationTime);
            if (Sim.pumpStand.aPumpIsAvailable()) {
                Pump p = Sim.pumpStand.takeAvailablePump();
                if (p != null) p.startService(arrivingCar);
            } else {
                Sim.carQueue.insert(arrivingCar);
            }
        }

        // Schedule the next arrival, reusing the current event object.
        setTime(Sim.simulationTime + interarrivalTime());
        Sim.eventList.insert(this);
    }
}
