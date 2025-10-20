package sim;

/**
 * Car: the class representing cars.
 */
public class Car {
    private double arrivalTime;
    private final double litresNeeded;

    /**
     * The number of litres required is a property of a car, so it belongs in this class.
     * It is also something the car "knows" when it arrives, so it should be calculated in the constructor.
     * The distribution of litres required is uniform between 10 and 60.
     */
    public Car() {
        this.litresNeeded = Sim.litresNeededMin + Sim.litreStream.nextDouble() * Sim.litresNeededRange;
    }

    /** return the car's arrival time. */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /** return the number of litres of fuel needed by the car. */
    public double getLitresNeeded() {
        return litresNeeded;
    }

    /** set the car's arrival time. */
    public void setArrivalTime(double time) {
        this.arrivalTime = time;
    }
}
