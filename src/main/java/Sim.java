import java.util.*;
import java.io.*;

/**
 * CSC 270 simulation example
 * Adapted January 1998 by J. Clarke from a C++ version, itself based on a
 * Turing original by M. Molle
 *
 * Sim: the class in charge of the simulation.
 * This class contains both the main() method for the application, and the
 * "global" variables controlling the execution.
 */
public class Sim {

    /** Global quantities used throughout the simulation */
    public static double simulationTime;         // What time is it?
    public static double reportInterval;         // How often should we report?

    // quantities that determine how we model the real world
    // In a more elaborate program, these might be input data.

    // economics: profit per litre of gas, and cost to operate one pump for a day
    public static double profit = 0.025;
    public static double pumpCost = 20.0;

    // demand: minimum and maximum amount of gas needed by a car (see Car constructor)
    public static double litresNeededMin = 10.0;
    public static double litresNeededRange = 50.0; // => uniform [10, 60)

    // service times: constant base time + time per litre + random spread (see Pump.serviceTime)
    public static double serviceTimeBase = 150.0;
    public static double serviceTimePerLitre = 0.5;
    public static double serviceTimeSpread = 30.0;

    // customer behaviours: probability of balking depends on three ad-hoc constants (see Arrival.doesCarBalk)
    public static double balkA = 40.0;
    public static double balkB = 25.0;
    public static double balkC = 3.0;

    // customer arrival rate (see Arrival.interarrivalTime).
    public static double meaninterarrivalTime = 50.0; // seconds

    // random-number streams used to model the world
    public static Random arrivalStream;  // auto arrival times
    public static Random litreStream;    // number of litres needed
    public static Random balkingStream;  // balking probability
    public static Random serviceStream;  // service times

    // major data structures
    public static EventList eventList;
    public static CarQueue carQueue;
    public static PumpStand pumpStand;
    public static Statistics stats;

    /**
     * main entrypoint - starts the application
     */
    public static void main(String[] args) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

        // Read data and print introduction.
        // Inputs (each on its own line):
        //   reportInterval (double)
        //   endingTime (double)
        //   numPumps (int)
        //   seed for arrivalStream (int)
        //   seed for litreStream (int)
        //   seed for balkingStream (int)
        //   seed for serviceStream (int)
        reportInterval = Double.parseDouble(in.readLine().trim());
        double endingTime = Double.parseDouble(in.readLine().trim());
        int numPumps = Integer.parseInt(in.readLine().trim());

        System.out.print("This simulation run uses " + numPumps + " pumps");
        System.out.println(" and the following random number seeds:");

        int seed = Integer.parseInt(in.readLine().trim());
        arrivalStream = new Random(seed);
        System.out.print(" " + seed);

        seed = Integer.parseInt(in.readLine().trim());
        litreStream = new Random(seed);
        System.out.print(" " + seed);

        seed = Integer.parseInt(in.readLine().trim());
        balkingStream = new Random(seed);
        System.out.print(" " + seed);

        seed = Integer.parseInt(in.readLine().trim());
        serviceStream = new Random(seed);
        System.out.print(" " + seed);
        System.out.println();

        // Create and initialize the event list, the car queue, the pump stand, and the statistics collector.
        eventList = new EventList();
        carQueue = new CarQueue();
        pumpStand = new PumpStand(numPumps);
        stats = new Statistics();

        // Schedule the required events:
        //   the end of the simulation;
        //   the first progress report;
        //   the arrival of the first car.
        EndOfSimulation lastEvent = new EndOfSimulation(endingTime);
        eventList.insert(lastEvent);

        if (reportInterval <= endingTime && reportInterval > 0) {
            Report nextReport = new Report(reportInterval);
            eventList.insert(nextReport);
        }

        // (Should the first car really arrive at time 0?)
        eventList.insert(new Arrival(0.0));

        // The "clock driver" loop
        while (true) {
            Event currentEvent = eventList.takeNextEvent();
            if (currentEvent == null) {
                System.out.println("Error! ran out of events");
                break;
            }
            simulationTime = currentEvent.getTime();
            currentEvent.makeItHappen();
            if (currentEvent instanceof EndOfSimulation) break;
        }
    }
}

/**
 * Statistics: the class for objects that collect statistics.
 * (There is only one such object in this program.)
 */
class Statistics {
    // The explicit initializations are not needed, but improve clarity.
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

/**
 * Car: the class representing cars.
 */
class Car {
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

/**
 * CarQueue: the class representing the lineup of cars at the gas station.
 */
class CarQueue {

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

/**
 * Event: the class representing events within the simulation model.
 */
abstract class Event {
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

/**
 * EventList: the class for the event list.
 * (There is only one object of this class in the program.)
 */
class EventList {

    /** ListItem: the class for objects stored in the event list. */
    private static class ListItem {
        public Event data;
        public ListItem next;
    }

    private ListItem firstEvent = null; // head of time-ordered list

    /** insert: add an event e to the event list in the appropriate place, prioritized by time. */
    public void insert(Event e) {
        ListItem item = new ListItem();
        item.data = e;

        final double time = e.getTime();
        if (firstEvent == null || time < firstEvent.data.getTime()) {
            item.next = firstEvent;
            firstEvent = item;
        } else {
            ListItem behind = firstEvent;
            ListItem ahead = firstEvent.next;
            while (ahead != null && ahead.data.getTime() <= time) {
                behind = ahead;
                ahead = ahead.next;
            }
            behind.next = item;
            item.next = ahead;
        }
    }

    /** takeNextEvent: remove the item at the head of the event list and return it. */
    public Event takeNextEvent() {
        if (firstEvent == null) return null;
        Event eventToReturn = firstEvent.data;
        firstEvent = firstEvent.next;
        return eventToReturn;
    }
}

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

