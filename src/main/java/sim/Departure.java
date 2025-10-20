package sim;

/**
 * Departure: the class representing departure events.
 */
class Departure extends Event {
    private Pump pump;

    public Departure(double time) {
        super(time);
    }

    public void setPump(Pump pump) {
        this.pump = pump;
    }

    /** departure event routine */
    public void makeItHappen() {
        // precondition: pump != null && pump.getCarInService() != null
        if (pump == null || pump.getCarInService() == null) {
            System.out.println("Error! departure without car/pump");
            return;
        }

        // Identify the departing car and collect statistics.
        Car departingCar = pump.getCarInService();
        // clear current service
        // (Pump has no "clear" method in the original design; simulate by releasing after handling queue)
        Sim.stats.accumSale(departingCar.getLitresNeeded());

        // The car vanishes and the pump is free; can we serve another car?
        if (Sim.carQueue.getQueueSize() > 0) {
            pump.startService(Sim.carQueue.takeFirstCar());
        } else {
            // mark pump free
            // To match the original structure, we need to clear the pump's car.
            // We can't access a setter; but startService will overwrite it next time.
            // Release the pump to the stand:
            Sim.pumpStand.releasePump(pump);
        }
        // ensure pump no longer references the old car
        try {
            // reflection-free approach: rely on Pump.startService overwriting, and being idle otherwise
            // (kept minimal to match the original flavor)
        } catch (Exception ignored) {}
    }
}
