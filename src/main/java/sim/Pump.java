package sim;

/**
 * Pump: the class representing single pumps at the gas station.
 */
class Pump {
    private Car carInService = null;

    /** get the car currently being served by the pump. */
    public Car getCarInService() {
        return carInService;
    }

    /**
     * serviceTime: determine how long the service will take.
     * Service times have a normal distribution with a mean given by a constant base
     * plus an amount of time per litre, and with a fixed standard deviation.
     */
    private double serviceTime() {
        if (carInService == null) {
            System.out.println("Error! no car in service when expected");
            return -1.0;
        }
        return Sim.serviceTimeBase
                + Sim.serviceTimePerLitre * carInService.getLitresNeeded()
                + Sim.serviceTimeSpread * Sim.serviceStream.nextGaussian();
    }

    /**
     * startService: the start-of-service event routine.
     * Connects the car to this pump, and determines when the service will stop.
     */
    public void startService(Car car) {
        // precondition: Sim.pumpStand.aPumpIsAvailable()
        carInService = car;
        final double pumpTime = Math.max(0.0, serviceTime());

        // Collect statistics.
        Sim.stats.accumWaitingTime(Sim.simulationTime - carInService.getArrivalTime());
        Sim.stats.accumServiceTime(pumpTime);

        // Schedule departure of car from this pump.
        Departure dep = new Departure(Sim.simulationTime + pumpTime);
        dep.setPump(this);
        Sim.eventList.insert(dep);
    }
}
