package sim;

/**
 * Event: the class representing events within the simulation model.
 */
public abstract class Event {
    private double time; // the time when the event happens

    public Event(double time) {
        this.time = time;
    }

    /** return the time of the event. */
    public double getTime() {
        return time;
    }

    /** the event routine. */
    public abstract void makeItHappen();

    /** set the time of the event. */
    public void setTime(double time) {
        this.time = time;
    }
}
