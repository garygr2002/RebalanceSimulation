package com.garygregg.rebalance.simulation.simulation;

import com.garygregg.rebalance.simulation.data.Database;
import com.garygregg.rebalance.simulation.data.MonthlyData;
import com.garygregg.rebalance.simulation.fund.Portfolio;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class RebalanceSimulation {

    // The bear market ratio
    private static final double bearRatio = 0.8;

    // The initial high of the S&P 500
    private static final double initialHigh = 71.74;

    // Our locale
    private static final Locale locale = Locale.getDefault();

    // Our currency formatter
    private static final NumberFormat currencyFormatter =
            NumberFormat.getCurrencyInstance(locale);
    // The zeroth simulation
    private static final Runnable simulation0 =
            RebalanceSimulation::runSimulation0;
    /*
     * The remainder of a portfolio not allocated to stocks that is allocated
     * to bonds
     */
    private static final double remainderFractionBond = 2. / 3.;

    // Our random number generator
    private static final Random rng = new Random(0x74969d18);

    // The highest stock allocation with which we are comfortable
    private static final double stockHigh = 0.7;

    // For floating-point output rounded to two places
    private static final DecimalFormat twoDigitFormat =
            new DecimalFormat("0.00");

    // The start of the simulation run
    private static Date start;

    // The first simulation
    private static final Runnable simulation1 =
            RebalanceSimulation::runSimulation1;

    // The second simulation
    private static final Runnable simulation2 =
            RebalanceSimulation::runSimulation2;

    // The third simulation
    private static final Runnable simulation3 =
            RebalanceSimulation::runSimulation3;

    // An array with the simulations
    private static final Runnable[] simulations =
            new Runnable[]{simulation0, simulation1, simulation2, simulation3};

    // A hyperbolic adjuster for rebalancing
    private final HyperbolicAdjuster adjuster = new HyperbolicAdjuster();

    // A calendar for managing dates
    private final Calendar calendar = Calendar.getInstance();

    // The database instance
    private final Database database = Database.getInstance();

    // Our portfolio
    private final Portfolio portfolio = new Portfolio();

    // The advance threshold for rebalancing
    private double advanceThreshold;

    // The bear market allocation
    private double bear;

    // The decline threshold for rebalancing
    private double declineThreshold;

    // The discretionary annual percent of the portfolio for withdraw
    private double discretionary = 0.04 /
            Database.getMonthCount();

    // The discretionary draw portion
    private double discretionaryPortion;

    // The end date for the simulation
    private Date end;

    // The fixed annual percent of the portfolio for withdraw (with COLA)
    private double fixed = 0.04 / Database.getMonthCount();

    // The fixed draw portion
    private double fixedPortion;

    // The high market allocation
    private double high;

    // The total draw from the simulation
    private double totalDraw;

    // The zero market allocation
    private double zero;

    {
        setX(initialHigh);
    }

    /**
     * Returns the count of simulations.
     *
     * @return The count of simulations
     */
    public static int getSimulationCount() {
        return simulations.length;
    }

    /**
     * Tests the simulation.
     *
     * @param arguments Command line arguments
     */
    public static void main(@NotNull String @NotNull [] arguments) {

        // Is there at least one argument?
        if (0 < arguments.length) {

            /*
             * There is at least one argument. Declare and initialize a
             * simulation index.
             */
            int simulationIndex = 0;
            try {

                /*
                 * Try to parse the simulation index from the first command
                 * line argument, and run the corresponding simulation.
                 */
                simulations[simulationIndex =
                        Integer.parseInt(arguments[0])].run();
            }

            /*
             * Catch an exception resulting from simulation not corresponding
             * to the given index.
             */ catch (@NotNull ArrayIndexOutOfBoundsException exception) {
                System.err.printf("Cannot call simulation with index %d; " +
                                "there are only %d simulations.%n",
                        simulationIndex, getSimulationCount());
            }

            /*
             * Catch an exception resulting from the first argument not
             * parseable as an integer.
             */ catch (@NotNull NumberFormatException exception) {
                System.err.printf("The argument '%s' cannot be parsed as a " +
                        "simulation index.%n", arguments[0]);
            }
        }

        // There are no arguments.
        else {
            System.err.printf("The rebalance simulation requires a integer " +
                            "argument between 0 and %d.%n",
                    getSimulationCount());
        }
    }

    /**
     * Runs a simulation, and write about the result.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void runAndWrite(@NotNull RebalanceSimulation simulation,
                                    @NotNull FileWriter writer)
            throws IOException {

        /*
         * Clear the draw in the simulation. Run the simulation, and output
         * results.
         */
        simulation.clearDraw();
        writer.write(String.format("%.3f,%.3f,%.3f,%.3f,%.3f,%.3f,%s,%s\n",
                simulation.getDiscretionary(),
                simulation.getFixed(),
                simulation.getHigh(),
                simulation.getBear(),
                simulation.getAdvanceThreshold(),
                simulation.getDeclineThreshold(),
                twoDigitFormat.format(simulation.run(false)),
                twoDigitFormat.format(simulation.getDraw())));
    }

    /**
     * Runs simulations by randomly choosing the high and bear parameters
     * within a range, this some fixed number of times.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void runRandomHighAndBear(
            @NotNull RebalanceSimulation simulation,
            @NotNull FileWriter writer) throws IOException {

        // Declare local variables.
        double high;
        Date now;

        // Cycle the required number of iterations.
        final int reportThreshold = 15840, iterations = reportThreshold * 100;
        for (int i = 0; i < iterations; ++i) {

            // Generate, and set a random market high.
            high = rng.nextDouble() * 0.15 + 0.40;
            simulation.setHigh(high);

            // Generate, and set a market bear.
            simulation.setBear(rng.nextDouble() * (simulation.getZero() - high)
                    + high);

            /*
             * Run the simulation, and write about it. Is the iteration count
             * at a report threshold?
             */
            runAndWrite(simulation, writer);
            if (0 == (i % reportThreshold)) {

                /*
                 * The iteration count is at a report threshold. Do a
                 * checkpoint.
                 */
                now = new Date();
                System.out.printf("Iteration %d; Time: %s seconds%n", i,
                        twoDigitFormat.format((now.getTime() -
                                start.getTime()) / 1000.));
            }
        }
    }

    /**
     * Runs one simulation.
     */
    @SuppressWarnings("unused")
    private static void runSimulation() {

        /*
         * Create a rebalance simulation. Get the count of months.
         */
        final RebalanceSimulation simulation = new RebalanceSimulation();
        final int monthCount = Database.getMonthCount();

        // Set discretionary and fixed.
        simulation.setDiscretionary(0.018);
        simulation.setFixed(0.018);

        // Set the rebalance thresholds.
        simulation.setAdvanceThreshold(0.10);
        simulation.setDeclineThreshold(-0.03);

        // Set the high market allocation and the zero market allocation.
        simulation.setHigh(0.45);
        simulation.setZero(0.70);

        // Set the bear market allocation, and run the simulation.
        simulation.setBear(0.575);
        simulation.run(true);

        // Display the total draw.
        System.out.printf("The total draw is: %s%n",
                currencyFormatter.format(simulation.getDraw()));
    }

    /**
     * Runs the zeroth simulation.
     */
    private static void runSimulation0() {
        runSimulation();
    }

    /**
     * Runs the first simulation.
     */
    private static void runSimulation1() {
        runSimulations(false);
    }

    /**
     * Runs the second simulation.
     */
    private static void runSimulation2() {
        runSimulations(true);
    }

    /**
     * Runs the third simulation.
     */
    private static void runSimulation3() {

        /*
         * Create a simulation object. Set the advance and decline thresholds
         * to invariate values.
         */
        final RebalanceSimulation simulation = new RebalanceSimulation();
        simulation.setAdvanceThreshold(0.11);
        simulation.setDeclineThreshold(-0.10);

        /*
         * Set the discretionary, fixed and zero parameters of the simulation
         * to invariate values.
         */
        simulation.setDiscretionary(0.024);
        simulation.setFixed(0.015);
        simulation.setZero(stockHigh + 0.02);

        // Try to create a file writer.
        try (final FileWriter writer = new FileWriter(new File("data",
                "rebalance_simulation.csv"))) {

            // Write the header.
            writer.write("discretionary,fixed,high,bear,advance,decline," +
                    "value,draw\n");

            // Run the simulation by randomly varying the high and bear values.
            start = new Date();
            runRandomHighAndBear(simulation, writer);
        }

        /*
         * Catch any I/O exception that may occur, and output a message to
         * system error.
         */ catch (IOException exception) {
            System.err.printf("A writer for the simulations could not be " +
                    "created; message is: '%s'.", exception.getMessage());
        }
    }

    /**
     * Runs a series of simulations.
     *
     * @param wideSpan True if the range of discretionary value should be
     *                 'wide'; false otherwise
     */
    private static void runSimulations(boolean wideSpan) {

        // Create a simulation object and set its invariate parameters.
        final RebalanceSimulation simulation = new RebalanceSimulation();
        simulation.setZero(stockHigh + 0.02);

        // Try to create a file writer, then run the simulation.
        try {

            // Create the file writer
            final FileWriter writer = new FileWriter(new File("data",
                    "rebalance_simulation.csv"));

            // Write the header.
            writer.write("discretionary,fixed,high,bear,advance,decline," +
                    "value,draw\n");

            // Run the simulations by varying discretionary. Close the writer.
            start = new Date();
            varyDiscretionary(simulation, writer, wideSpan);
            writer.close();
        }

        /*
         * Catch any I/O exception that may occur, and output a message to
         * system error.
         */ catch (IOException exception) {
            System.err.printf("A writer for the simulations could not be " +
                    "created; message is: '%s'.", exception.getMessage());
        }
    }

    /**
     * Varies the advance threshold.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyAdvance(@NotNull RebalanceSimulation simulation,
                                    @NotNull FileWriter writer)
            throws IOException {

        // Cycle through the well-known advance threshold range.
        for (double advanceThreshold = 0.06; advanceThreshold < 0.11;
             advanceThreshold += 0.01) {

            // Set the advance threshold, and vary the decline threshold.
            simulation.setAdvanceThreshold(advanceThreshold);
            varyDecline(simulation, writer);
        }
    }

    /**
     * Varies the bear market.
     *
     * @param simulation The simulation to run
     * @param start      The start of the bear market parameter (inclusive)
     * @param end        The end of the bear market parameter (exclusive)
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyBear(@NotNull RebalanceSimulation simulation,
                                 double start,
                                 @SuppressWarnings("SameParameterValue")
                                 double end,
                                 @NotNull FileWriter writer)
            throws IOException {

        // Cycle through the given bear market range.
        for (double bear = start; bear < end; bear += 0.1) {

            // Set the bear market, and vary the advance threshold.
            simulation.setBear(bear);
            varyAdvance(simulation, writer);
        }
    }

    /**
     * Varies the decline threshold and runs the simulation.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyDecline(@NotNull RebalanceSimulation simulation,
                                    @NotNull FileWriter writer)
            throws IOException {

        // Cycle through the well-known decline threshold range.
        for (double declineThreshold = -0.02; declineThreshold > -0.12;
             declineThreshold -= 0.01) {

            /*
             * Set the decline threshold. Run the simulation, and write about
             * it.
             */
            simulation.setDeclineThreshold(declineThreshold);
            runAndWrite(simulation, writer);
        }
    }

    /**
     * Varies discretionary.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @param wideSpan   True if the range of discretionary value should be
     *                   'wide'; false otherwise
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyDiscretionary(
            @NotNull RebalanceSimulation simulation,
            @NotNull FileWriter writer,
            boolean wideSpan)
            throws IOException {

        /*
         * Declare and initialize the increment and the start based on the
         * wide-span argument.
         */
        final double increment = wideSpan ? 0.00125 : 0.0005;
        final double start = wideSpan ? 0. : 0.015;

        // Cycle through the well-known discretionary range.
        for (double discretionary = start; discretionary < 0.025;
             discretionary += increment) {

            // Set the discretionary, and vary fixed.
            simulation.setDiscretionary(discretionary);
            varyFixed(simulation, writer, wideSpan);
        }
    }

    /**
     * Varies fixed.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @param wideSpan   True if the range of fixed values should be 'wide';
     *                   false otherwise
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyFixed(@NotNull RebalanceSimulation simulation,
                                  @NotNull FileWriter writer,
                                  boolean wideSpan)
            throws IOException {

        /*
         * Declare and initialize the increment and the start range based on
         * the wide-span argument.
         */
        final double increment = wideSpan ? 0.00125 : 0.0005;
        final double startRange = wideSpan ? 0. : 0.015;

        // Cycle through the well-known fixed range.
        Date now;
        for (double fixed = startRange; fixed < 0.025; fixed += increment) {

            // Set the fixed, and vary high.
            simulation.setFixed(fixed);
            varyHigh(simulation, writer);

            // Do a checkpoint.
            now = new Date();
            System.out.printf("Discretionary: %f; Fixed: %f; Elapsed Time: " +
                            "%s seconds%n",
                    simulation.getDiscretionary(), simulation.getFixed(),
                    twoDigitFormat.format((now.getTime() - start.getTime()) /
                            1000.));
        }
    }

    /**
     * Varies the high market.
     *
     * @param simulation The simulation to run
     * @param writer     A writer to receive simulation parameters and output
     * @throws IOException Indicates simulation parameters and output could not
     *                     be written
     */
    private static void varyHigh(@NotNull RebalanceSimulation simulation,
                                 @NotNull FileWriter writer)
            throws IOException {

        // Declare constants.
        final double offset = 0.01, highEnd = stockHigh,
                bearEnd = highEnd + offset;

        // Cycle through the well-known high market range.
        for (double high = 0.4, bear = high + offset; high < highEnd;
             high = bear, bear = high + offset) {

            // Set the high market, and vary the bear market.
            simulation.setHigh(high);
            varyBear(simulation, bear, bearEnd, writer);
        }
    }

    /**
     * Adds a draw to the total draw.
     *
     * @param draw An amount to add to the total draw
     */
    private void addDraw(double draw) {
        totalDraw += draw;
    }

    /**
     * Checks the current S&P 500 against the known high.
     *
     * @param high The current S&P 500
     */
    private void checkHigh(double high) {

        // Set a new high, as necessary.
        if (adjuster.getXHigh() < high) {
            adjuster.setX(high, high * bearRatio, 0.);
        }
    }

    /**
     * Clears the total draw.
     */
    private void clearDraw() {
        totalDraw = 0.;
    }

    /**
     * Gets the advance threshold for rebalancing.
     *
     * @return The advance threshold for rebalancing
     */
    public double getAdvanceThreshold() {
        return advanceThreshold;
    }

    /**
     * Gets the bear market allocation.
     *
     * @return The bear market allocation
     */
    public double getBear() {
        return bear;
    }

    /**
     * Gets the decline threshold for rebalancing.
     *
     * @return The decline threshold for rebalancing
     */
    public double getDeclineThreshold() {
        return declineThreshold;
    }

    /**
     * Gets the discretionary annual percent of the portfolio for withdraw.
     *
     * @return The discretionary annual percent of the portfolio
     * for withdraw
     */
    public double getDiscretionary() {
        return discretionary;
    }

    /**
     * Gets the total draw.
     *
     * @return The total draw
     */
    public double getDraw() {
        return totalDraw;
    }

    /**
     * Gets the fixed annual percent of the portfolio for withdraw (with COLA).
     *
     * @return The fixed annual percent of the portfolio for withdraw
     * (with COLA)
     */
    public double getFixed() {
        return fixed;
    }

    /**
     * Gets the high market allocation.
     *
     * @return The high market allocation
     */
    public double getHigh() {
        return high;
    }

    /**
     * Gets the calendar month.
     *
     * @return The calendar month
     */
    private int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    /**
     * Gets the calendar year.
     *
     * @return The calendar year
     */
    private int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    /**
     * Sets the zero market allocation.
     *
     * @return The zero market allocation
     */
    public double getZero() {
        return zero;
    }

    /**
     * Increments the calendar month.
     */
    private void incrementMonth() {
        calendar.add(Calendar.MONTH, 1);
    }

    /**
     * Initializes the calendar.
     */
    private void initializeCalendar() {

        // Is the end date not set? Note: This is a lazy initialization.
        if (null == end) {

            /*
             * The end date is not set. Clear the calendar. Set the end date in
             * the calendar, and use the calendar to set the end date.
             */
            calendar.clear();
            calendar.set(2022, Calendar.JANUARY, 1, 0, 0, 0);
            end = calendar.getTime();
        }

        // Clear the calendar, and set the start date.
        calendar.clear();
        calendar.set(1962, Calendar.JANUARY, 1, 0, 0, 0);
    }

    /**
     * Determine if the calendar is not at the end.
     *
     * @return True if the calendar is not at the end; false if it is
     */
    private boolean isNotAtEnd() {
        return calendar.getTime().before(end);
    }

    /**
     * Runs the simulation.
     *
     * @param noisy True if the simulation should report statistics; false
     *              otherwise
     * @return The value of the portfolio at the end of the simulation
     */
    @SuppressWarnings("UnusedReturnValue")
    private double run(@SuppressWarnings("SameParameterValue") boolean noisy) {

        /*
         * Set the 'x' values of the adjuster based on the initial market high.
         * Set the desired market high, market bear, and market zero equity
         * allocations in the adjuster.
         */
        setX(initialHigh);
        adjuster.setY(getHigh(), getBear(), getZero());

        /*
         * Set the initial value of the portfolio, and update its
         * discretionary portion.
         */
        portfolio.setValue(1000000.0);
        updateDiscretionaryPortion();

        // Initialize the fixed portion of the draw. Clear the total draw.
        fixedPortion = portfolio.getValue() * (getFixed() /
                Database.getMonthCount());
        clearDraw();

        /*
         * Initialize the calendar. Get the data for the first month of the
         * simulation.
         */
        initializeCalendar();
        MonthlyData data = database.getEntry(getYear(), getMonth());

        /*
         * Declare a variable to receive a draw, and another to receive a
         * message.
         */
        double draw;
        String message;

        /*
         * Get the data for the value of the S&P 500 at the start of the
         * simulation. Clear the counts of portfolio rebalances.
         */
        double sAndP500 = data.getSAndP500ValueStart();
        portfolio.resetCounts();

        /*
         * Do an unconditional rebalance of the portfolio, and cycle while the
         * simulation is not at an end. Note: This initial rebalance should
         * result in one initial too-low rebalance.
         */
        portfolio.rebalance(0., 0., adjuster.f(sAndP500),
                remainderFractionBond);
        while (isNotAtEnd()) {

            /*
             * Check the value of the S&P 500 at the close of the month against
             * the known high. Calculate the draw, and add it to the total
             * draw.
             */
            checkHigh(sAndP500 = data.getSAndP500ValueEnd());
            draw = discretionaryPortion + fixedPortion;
            addDraw(draw);

            /*
             * Update the value of the portfolio using the monthly data and the
             * draw. Are we noisy?
             */
            portfolio.incrementMonth(data, -draw, getAdvanceThreshold(),
                    getDeclineThreshold(), adjuster.f(sAndP500),
                    remainderFractionBond);
            if (noisy) {

                // We are noisy. Format a message.
                message = String.format("At the end of %s, %d, the value " +
                                "of the portfolio was: ",
                        calendar.getDisplayName(Calendar.MONTH, Calendar.LONG,
                                locale), calendar.get(Calendar.YEAR));

                // Say something about our valuation this month.
                System.out.printf("%-65s: %s%n", message,
                        currencyFormatter.format(portfolio.getValue()));
            }

            // Update the discretionary and fixed portions of the portfolio.
            updateDiscretionaryPortion();
            updateFixedPortion(data);

            // Increment the month, and get the database data for the month.
            incrementMonth();
            data = database.getEntry(getYear(), getMonth());
        }

        // Done. Are we noisy?
        if (noisy) {

            // We are noisy. Describe how many rebalances occurred.
            System.out.printf("Too low rebalances: %d; too high rebalances: %d%n",
                    portfolio.getRebalanceTooLow(),
                    portfolio.getRebalanceTooHigh());
        }

        // Return the value of the portfolio.
        return portfolio.getValue();
    }

    /**
     * Sets the advance threshold for rebalancing.
     *
     * @param advanceThreshold The advance threshold for rebalancing
     */
    public void setAdvanceThreshold(double advanceThreshold) {
        this.advanceThreshold = advanceThreshold;
    }

    /**
     * Sets the bear market allocation.
     *
     * @param bear The bear market allocation
     */
    public void setBear(double bear) {
        this.bear = bear;
    }

    /**
     * Sets the decline threshold for rebalancing.
     *
     * @param declineThreshold The decline threshold for rebalancing
     */
    public void setDeclineThreshold(double declineThreshold) {
        this.declineThreshold = declineThreshold;
    }

    /**
     * Sets the discretionary annual percent of the portfolio for withdraw.
     *
     * @param discretionary The discretionary annual percent of the portfolio
     *                      for withdraw
     */
    public void setDiscretionary(double discretionary) {
        this.discretionary = discretionary;
    }

    /**
     * Sets the fixed annual percent of the portfolio for withdraw (with COLA).
     *
     * @param fixed The fixed annual percent of the portfolio for withdraw
     *              (with COLA)
     */
    public void setFixed(double fixed) {
        this.fixed = fixed;
    }

    /**
     * Sets the high market allocation.
     *
     * @param high The high market allocation
     */
    public void setHigh(double high) {
        this.high = high;
    }

    /**
     * Sets the 'x' values of the adjuster.
     *
     * @param initialHigh The initial high for the adjuster
     */
    private void setX(@SuppressWarnings("SameParameterValue")
                      double initialHigh) {

        // Reinitialize the adjuster, and set the initial market high.
        adjuster.setX(0., 0., 0.);
        checkHigh(initialHigh);
    }

    /**
     * Sets the zero market allocation.
     *
     * @param zero The zero market allocation
     */
    public void setZero(double zero) {
        this.zero = zero;
    }

    /**
     * Updates the discretionary portion.
     */
    private void updateDiscretionaryPortion() {

        // Update the discretionary portion using the value of the portfolio.
        discretionaryPortion = portfolio.getValue() * (getDiscretionary() /
                Database.getMonthCount());
    }

    /**
     * Updates the fixed portion.
     */
    private void updateFixedPortion(@NotNull MonthlyData data) {
        fixedPortion *= data.getInflation();
    }
}
