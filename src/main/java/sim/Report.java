package sim;

/**
 * Report: the class representing reporting events.
 */
class Report extends Event {
    public Report(double time) {
        super(time);
    }

    /** interim reporting event routine. */
    public void makeItHappen() {
        Sim.stats.snapshot();
        // Schedule the next interim report.
        setTime(Sim.simulationTime + Sim.reportInterval);
        if (getTime() <= Sim.simulationTime) return; // guard
        Sim.eventList.insert(this);
    }
}
