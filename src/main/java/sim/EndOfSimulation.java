package sim;

/**
 * EndOfSimulation: the class representing the final event that stops the simulation.
 */
class EndOfSimulation extends Event {
    public EndOfSimulation(double time) {
        super(time);
    }

    public void makeItHappen() {
        // print a final snapshot
        Sim.stats.snapshot();
        // nothing else; main loop will stop after this event returns
    }
}
