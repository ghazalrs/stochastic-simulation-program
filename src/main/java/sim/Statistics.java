package sim;

/**
 * Statistics: the class for objects that collect statistics.
 * (There is only one such object in this program.)
 */
class Statistics {
    // The explicit initializations are not needed, but improve clarity.
    // Metrics
    private int totalArrivals = 0;
    private int customersServed = 0;
    private int balkingCustomers = 0;

    private double totalLitresSold = 0.0;
    private double totalLitresMissed = 0.0;

    private double totalWaitingTime = 0.0;
    private double totalServiceTime = 0.0;

    /**
     * Constructor.
     */
    public Statistics() {
        printHeaders();
    }

    // Methods to update metrics, called from events
    /**
     * accumBalk: record and count a lost sale.
     */
    public void accumBalk(double litres) {
        balkingCustomers += 1;
        totalLitresMissed += litres;
    }

    /**
     * accumSale: record and count a sale.
     */
    public void accumSale(double litres) {
        customersServed += 1;
        totalLitresSold += litres;
    }

    /**
     * accumServiceTime: record a customer's service time.
     */
    public void accumServiceTime(double interval) {
        totalServiceTime += interval;
    }

    /**
     * accumWaitingTime: record a customer's waiting time.
     */
    public void accumWaitingTime(double interval) {
        totalWaitingTime += interval;
    }

    /**
     * countArrival: record an arrival.
     */
    public void countArrival() {
        totalArrivals += 1;
    }

    // Formatting helpers to keep the report columns aligned in plain text
    /**
     * fmtDbl: convert a double to a string of a specified width representing
     * the number rounded to the specified number of digits. The string
     * returned is padded by blanks on the left if necessary. If it is too long,
     * it is not changed.
     */
    private static String fmtDbl(double number, int width, int precision) {
        double scale = 1.0;
        for (int i = 0; i < precision; i++) scale *= 10.0;
        String result = "" + (int) (number * scale + 0.5);

        if (precision > 0) {
            for (int i = result.length(); i < precision + 1; i++) result = "0" + result;
            int insertPos = result.length() - precision; // where the decimal point goes
            result = result.substring(0, insertPos) + "." + result.substring(insertPos);
        }
        for (int i = result.length(); i < width; i++) result = " " + result;
        return result;
    }

    /**
     * fmtInt: convert an int to a string of a specified width.
     * The string returned is padded by blanks on the left if necessary.
     * If it is too long, it is not changed.
     */
    private static String fmtInt(int number, int width) {
        String result = "" + number;
        for (int i = result.length(); i < width; i++) result = " " + result;
        return result;
    }

    /** printHeaders: print column titles for the statistics summaries. */
    private static void printHeaders() {
        System.out.println(" Current  Total  NoQueue  Car->Car  Average  Number  Average  Pump   Total     Lost");
        System.out.println("  Time     Cars  Fraction    Time    Litres  Balked   Wait    Usage  Profit   Profit");
        for (int i = 0; i < 79; i++) System.out.print("-");
        System.out.println();
    }

    /** snapshot: print a summary of the statistics so far. */
    public void snapshot() {
        System.out.print(fmtDbl(Sim.simulationTime, 8, 0));
        System.out.print(fmtInt(totalArrivals, 7));

        double noQueueFrac = (Sim.simulationTime > 0.0)
                ? (Sim.carQueue.getEmptyTime() / Sim.simulationTime) : 0.0;
        System.out.print(fmtDbl(noQueueFrac, 8, 3));

        if (totalArrivals > 0) {
            System.out.print(fmtDbl(Sim.simulationTime / totalArrivals, 9, 3));
            System.out.print(fmtDbl((totalLitresSold + totalLitresMissed) / totalArrivals, 10, 3));
        } else {
            System.out.print("   Unknown");
            System.out.print("   Unknown");
        }

        System.out.print(fmtInt(balkingCustomers, 8));
        if (customersServed > 0)
            System.out.print(fmtDbl(totalWaitingTime / customersServed, 9, 3));
        else
            System.out.print("   Unknown");

        double denom = Sim.pumpStand.getNumberOfPumps() * Math.max(1e-9, Sim.simulationTime);
        System.out.print(fmtDbl(totalServiceTime / denom, 8, 3));

        double totalProfit = (totalLitresSold * Sim.profit) - (Sim.pumpCost * Sim.pumpStand.getNumberOfPumps());
        System.out.print(fmtDbl(totalProfit, 9, 2));
        System.out.print(fmtDbl(totalLitresMissed * Sim.profit, 9, 2));
        System.out.println();
    }
}
