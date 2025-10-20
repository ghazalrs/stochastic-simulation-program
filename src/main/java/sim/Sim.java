package sim;

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
    // separate streams for different random variables
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

        // 4 random number seeds (one for each stream)
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
        // Create core components (data structures)
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
        // Schedule the first arrival event at time 0
        eventList.insert(new Arrival(0.0));

        // The "clock driver" loop
        while (true) {
            // Fetch the earliest future event
            Event currentEvent = eventList.takeNextEvent();
            if (currentEvent == null) {
                System.out.println("Error! ran out of events");
                break;
            }

            // Each iteration of the loop jumps from one event to the next in the timeline
            // Set simulated time to the time of the event being processed
            simulationTime = currentEvent.getTime();
            // executes the event
            currentEvent.makeItHappen();
            if (currentEvent instanceof EndOfSimulation) break;
        }
        // The loop breaks if event list is empty or if the event being processed is an EndOfSimulation event
    }
}
