package sim;

/**
 * PumpStand: the class for the complete collection of pumps at the gas station.
 */
class PumpStand {
    private Pump[] pumps; // an array of pumps
    private int numPumps;
    private int topPump;

    /**
     * Constructor; build a PumpStand of numPumps pumps, and make all of them available.
     */
    public PumpStand(int numPumps) {
        if (numPumps < 1) {
            System.out.println("Error! pump stand needs more than 0 pumps");
            numPumps = 1;
        }
        pumps = new Pump[numPumps];
        this.numPumps = numPumps;
        topPump = numPumps - 1;
        for (int p = 0; p < numPumps; p++) pumps[p] = new Pump();
    }

    /** return true/false according to whether at least one pump is free for use. */
    public boolean aPumpIsAvailable() {
        return topPump >= 0;
    }

    /** return the number of pumps in the pump stand. */
    public int getNumberOfPumps() {
        return numPumps;
    }

    /** releasePump: put pump p back in the stock of available pumps. */
    public void releasePump(Pump p) {
        if (topPump >= numPumps - 1) {
            System.out.println("Error! attempt to release a free pump?");
            return;
        }
        pumps[++topPump] = p;
    }

    /** takeAvailablePump: take a pump from the set of free pumps, and return that pump. */
    public Pump takeAvailablePump() {
        if (topPump < 0) {
            System.out.println("Error! no pump available when needed");
            return null;
        }
        return pumps[topPump--];
    }
}
