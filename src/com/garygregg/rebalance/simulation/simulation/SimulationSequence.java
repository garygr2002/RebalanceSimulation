package com.garygregg.rebalance.simulation.simulation;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class SimulationSequence implements Runnable {

    /*
     * Parameter advancement order: discretionary, fixed, high, bear, advance,
     * decline.
     */

    // Default arguments
    private static final String[] defaultArguments = {"0",
            "RebalanceSimulation_in.csv", "RebalanceSimulation_out.csv",
            "RebalanceSimulation_win.csv"};

    // The number of default iterations
    private static final int defaultIterations = 1;

    // The default observation window for acceptable terminal portfolio value
    private static final Pair<Double, Double> defaultObservationWindow =
            new Pair<>(Double.MAX_VALUE, Double.MAX_VALUE);

    // The default output file name
    private static final String defaultOutput = "RebalanceSimulation_out.csv";

    // The invalid increment
    private static final double invalidIncrement = 0.;

    // Our directory for input and output
    private static final String ioDirectory = "data";

    // For floating-point output rounded to two places
    private static final DecimalFormat twoDigitFormat =
            new DecimalFormat("0.00");

    // The start date/time of the simulation run
    private static Date start;

    // Advance parameters
    private final StartEndIncrement advance = getInitial();

    // Bear parameters
    private final StartEndIncrement bear = getInitial();

    // Decline parameters
    private final StartEndIncrement decline = getInitial();

    // Discretionary parameters
    private final StartEndIncrement discretionary = getInitial();

    // Fixed parameters
    private final StartEndIncrement fixed = getInitial();

    // Our random number generator
    private final Random generator = new Random(0x385d3cf2);

    // High parameters
    private final StartEndIncrement high = getInitial();

    // Iteration counts at each level corresponding to parameters
    private final List<Integer> iterations = new ArrayList<>();

    // Our rebalance simulation
    private final RebalanceSimulation simulation = new RebalanceSimulation();

    // Zero parameters
    private final StartEndIncrement zero = getInitial();

    // The observation window for acceptable terminal portfolio value
    private Pair<Double, Double> observationWindow = defaultObservationWindow;

    // The number of desired simulations
    private int simulationCount;

    {
        initialize();
    }

    /**
     * Gets an argument from an argument array, or returns a default argument
     * if an explicit argument is not specified.
     *
     * @param arguments The argument array
     * @param index     The index of the argument to get
     * @return The specified argument, or a default argument
     * @throws IllegalArgumentException Indicates no explicit argument given,
     *                                  and no default argument available
     */
    @Contract(pure = true)
    private static @NotNull String getArgument(@NotNull String[] arguments,
                                               int index)
            throws IllegalArgumentException {

        // Get the argument for the specified index.
        final String argument = (0 <= index) ? ((index < arguments.length) ?
                arguments[index] : ((index < defaultArguments.length) ?
                defaultArguments[index] : null)) : null;

        /*
         * Throw an illegal argument exception if no explicit argument is
         * given, and there is no default argument available.
         */
        if (null == argument) {
            throw new IllegalArgumentException(String.format("No argument " +
                    "given for index %d, and no default available", index));
        }

        // Return the non-null argument.
        return argument;
    }

    /**
     * Gets a new, initialized start-end-increment.
     *
     * @return A new, initialized start-end-increment object.
     */
    private static @NotNull StartEndIncrement getInitial() {

        /*
         * Declare and initialize the initial value for all features of the
         * start-end-increment. Create a new start-end-increment object with
         * this value, and return the object.
         */
        final double initial = 0.;
        return new StartEndIncrement(initial, initial, initial);
    }

    /**
     * Determines if an increment is okay.
     *
     * @param increment The increment to test
     * @return True if the increment is okay; false otherwise
     */
    private static boolean incrementOkay(double increment) {
        return (invalidIncrement != increment);
    }

    /**
     * Tests this class.
     *
     * @param arguments Command line arguments
     */
    public static void main(@NotNull String @NotNull [] arguments) {

        /*
         * Try to do things that might throw an I/O exception, or number format
         * exception...
         */
        try {

            /*
             * ...and this basically just means getting the arguments, and
             * running the simulation.
             */
            run(Integer.parseInt(getArgument(arguments, 0)),
                    getArgument(arguments, 1),
                    getArgument(arguments, 2),
                    readWindow(getArgument(arguments, 3)));
        }

        /*
         * Catch any I/O exception or number format exception. Print the
         * message of the exception to system error stream.
         */ catch (@NotNull IOException |
                            @NotNull NumberFormatException exception) {
            System.err.println(exception.getMessage());
        }
    }

    /**
     * Reads a qualification window object from a well-formatted buffered
     * reader.
     *
     * @param reader A buffered reader
     * @return A qualification window object read from the reader
     * @throws ArrayIndexOutOfBoundsException Indicates that not all the
     *                                        required elements are in a line
     *                                        read from the reader
     * @throws IOException                    Indicates a problem reading from
     *                                        the reader
     * @throws NumberFormatException          Indicates a line element does
     *                                        not contain a well-formatted
     *                                        floating point number
     */
    private static @NotNull Pair<Double, Double>
    readWindow(@NotNull BufferedReader reader)
            throws ArrayIndexOutOfBoundsException, IOException,
            NumberFormatException {

        /*
         * Read a line from the reader, and split it around commas. Parse the
         * first two elements as doubles when creating the qualification window
         * object.
         */
        final String[] parameters = reader.readLine().split(",");
        return new Pair<>(Double.parseDouble(parameters[0].trim()),
                Double.parseDouble(parameters[1].trim()));
    }

    /**
     * Reads a qualification window object from a well-formatted file with the
     * given file name.
     *
     * @param filename The given file name.
     * @return A qualification window object read from the file
     * @throws ArrayIndexOutOfBoundsException Indicates that not all the
     *                                        required elements are in a line
     *                                        read from the reader
     * @throws IOException                    Indicates a problem reading from
     *                                        the reader
     * @throws NumberFormatException          Indicates a line element does
     *                                        not contain a well-formatted
     *                                        floating point number
     */
    private static @NotNull Pair<Double, Double>
    readWindow(@NotNull String filename) throws IOException {
        return readWindow(new BufferedReader(new FileReader(
                new File(ioDirectory, filename))));
    }

    /**
     * Runs a sequence of simulations.
     *
     * @param simulationCount The number of simulations to run
     */
    @SuppressWarnings("unused")
    private static void run(int simulationCount) {

        // Declare a simulation sequence, and invalidate all its increments.
        SimulationSequence sequence = new SimulationSequence();
        sequence.invalidateIncrements();

        // Set the discretionary start and the fixed start.
        sequence.setDiscretionaryStart(-0.0255);
        sequence.setFixedStart(-0.0145);

        // Set the advance start and the decline start.
        sequence.setAdvanceStart(0.10);
        sequence.setDeclineStart(-0.1211);

        // Set the bear start and the high start.
        sequence.setBearStart(0.58);
        sequence.setHighStart(0.48);

        // Set the zero start, and end-to-start.
        sequence.setZeroStart(0.68);
        sequence.setEndToStart();

        // Set the decline end.
        sequence.setDiscretionaryEnd(-0.0251);
        sequence.setFixedEnd(-0.0141);
        sequence.setDeclineEnd(-0.1205);

        // Set the simulation count, and run the sequence.
        sequence.setSimulationCount(1000000);
        sequence.run();
    }

    /**
     * Runs a sequence of simulations.
     *
     * @param simulationCount   The number of simulations to run
     * @param input             The name of the simulation input file
     * @param output            The name of the simulation output file
     * @param observationWindow The observation window that qualifies
     *                          termination portfolio value
     * @throws IOException Indicates a problem reading from the input file
     */
    private static void run(int simulationCount, @NotNull String input,
                            @NotNull String output,
                            @NotNull Pair<Double, Double> observationWindow)
            throws IOException {

        /*
         * Create a buffered reader from a file reader and the name of the
         * input file.
         */
        try (final BufferedReader reader = new BufferedReader(new FileReader(
                new File(ioDirectory, input)))) {

            /*
             * Create a simulation sequence object. Set the observation window
             * and the simulation count of the sequence.
             */
            final SimulationSequence sequence = new SimulationSequence();
            sequence.setObservationWindow(observationWindow);
            sequence.setSimulationCount(simulationCount);

            /*
             * Try to set the other parameters of the simulation sequence
             * object.
             */
            int line = 1;
            String parameter = "";
            try {

                // Set advance.
                ++line;
                parameter = "advance";
                setSei(sequence.advance, reader);

                // Set decline.
                ++line;
                parameter = "decline";
                setSei(sequence.decline, reader);

                // Set discretionary.
                ++line;
                parameter = "discretionary";
                setSei(sequence.discretionary, reader);

                // Set fixed.
                ++line;
                parameter = "fixed";
                setSei(sequence.fixed, reader);

                // Set high.
                ++line;
                parameter = "high";
                setSei(sequence.high, reader);

                // Set bear.
                ++line;
                parameter = "bear";
                setSei(sequence.bear, reader);

                // Set zero descriptors.
                ++line;
                parameter = "zero";

                // Set zero. Run the simulation.
                setSei(sequence.zero, reader);
                sequence.run(new File(ioDirectory, output));
            }

            /*
             * Catch any array-index-out-of-bounds, or number format exception
             * that may occur. Wrap a new I/O exception around a descriptive
             * message, and throw the I/O exception.
             */ catch (@NotNull ArrayIndexOutOfBoundsException |
                                @NotNull NumberFormatException exception) {
                throw new IOException(String.format("Parameter '%s' with" +
                                " message '%s' at line %d", parameter,
                        exception.getMessage(), line));
            }
        }
    }

    /**
     * Runs a simulation, and writes the result.
     *
     * @param simulation        The simulation to run
     * @param writer            A writer to receive output
     * @param format            The format for recording the simulation results
     * @param observationWindow The observation window for acceptable terminal
     *                          portfolio value
     * @return The number of simulation iterations to increment
     * @throws IOException Indicates an error writing output
     */
    private static int runAndWrite(
            @NotNull RebalanceSimulation simulation,
            @NotNull FileWriter writer, @NotNull String format,
            @NotNull Pair<Double, Double> observationWindow)
            throws IOException {

        /*
         * Run the simulation, receiving a portfolio terminal value. Determine
         * the number of simulation iterations to increment.
         */
        final double terminalValue = simulation.run(false);
        final int increment =
                ((terminalValue < observationWindow.getFirst()) ||
                        (observationWindow.getSecond() < terminalValue)) ? 0 : 1;

        // Is the number of iterations to increment greater than zero?
        if (0 < increment) {

            /*
             * The number of iterations to increment is greater than zero.
             * Record the simulation results.
             */
            writer.write(String.format(format,
                    simulation.getDiscretionary(),
                    simulation.getFixed(),
                    simulation.getHigh(),
                    simulation.getBear(),
                    simulation.getZero(),
                    simulation.getAdvanceThreshold(),
                    simulation.getDeclineThreshold(),
                    twoDigitFormat.format(terminalValue),
                    twoDigitFormat.format(simulation.getDraw())));
        }

        // Return the number of simulations to increment.
        return increment;
    }

    /**
     * Sets the end to start for a start-end-increment.
     *
     * @param sei A start-end-increment
     */
    private static void setEndToStart(@NotNull StartEndIncrement sei) {
        sei.setEnd(sei.getStart());
    }

    /**
     * Sets a start-end-increment object with input from a well-formatted
     * buffered reader.
     *
     * @param sei    The start-end-increment object
     * @param reader A buffered reader
     * @throws ArrayIndexOutOfBoundsException Indicates that not all the
     *                                        required elements are in a line
     *                                        read from the reader
     * @throws IOException                    Indicates a problem reading from
     *                                        the reader
     * @throws NumberFormatException          Indicates a line element does
     *                                        not contain a well-formatted
     *                                        floating point number
     */
    private static void setSei(@NotNull StartEndIncrement sei,
                               @NotNull BufferedReader reader)
            throws ArrayIndexOutOfBoundsException, IOException,
            NumberFormatException {

        /*
         * Read a line from the reader. Parse the trimmed start, and set it in
         * the start-end-increment.
         */
        final String[] parameters = reader.readLine().split(",");
        int element = 0;
        sei.setStart(Double.parseDouble(parameters[element++].trim()));

        /*
         * Parse the trimmed end, and the trimmed increment. Set both in the
         * start-end-increment.
         */
        sei.setEnd(Double.parseDouble(parameters[element++].trim()));
        sei.setIncrement(Double.parseDouble(parameters[element].trim()));
    }

    /**
     * Gets the advance end.
     *
     * @return The advance end
     */
    @SuppressWarnings("unused")
    public double getAdvanceEnd() {
        return advance.getEnd();
    }

    /**
     * Gets the advance increment.
     *
     * @return The advance increment
     */
    @SuppressWarnings("unused")
    public double getAdvanceIncrement() {
        return advance.getIncrement();
    }

    /**
     * Gets the advance start.
     *
     * @return The advance start
     */
    @SuppressWarnings("unused")
    public double getAdvanceStart() {
        return advance.getStart();
    }

    /**
     * Gets the bear end.
     *
     * @return The bear end
     */
    @SuppressWarnings("unused")
    public double getBearEnd() {
        return bear.getEnd();
    }

    /**
     * Gets the bear increment.
     *
     * @return The bear increment
     */
    @SuppressWarnings("unused")
    public double getBearIncrement() {
        return bear.getIncrement();
    }

    /**
     * Gets the bear start.
     *
     * @return The bear start
     */
    @SuppressWarnings("unused")
    public double getBearStart() {
        return bear.getStart();
    }

    /**
     * Gets the decline end.
     *
     * @return The decline end
     */
    @SuppressWarnings("unused")
    public double getDeclineEnd() {
        return decline.getEnd();
    }

    /**
     * Gets the decline increment.
     *
     * @return The decline increment
     */
    @SuppressWarnings("unused")
    public double getDeclineIncrement() {
        return decline.getIncrement();
    }

    /**
     * Gets the decline start.
     *
     * @return The decline start
     */
    @SuppressWarnings("unused")
    public double getDeclineStart() {
        return decline.getStart();
    }

    /**
     * Gets the discretionary end.
     *
     * @return The discretionary end
     */
    @SuppressWarnings("unused")
    public double getDiscretionaryEnd() {
        return discretionary.getEnd();
    }

    /**
     * Gets the discretionary increment.
     *
     * @return The discretionary increment
     */
    @SuppressWarnings("unused")
    public double getDiscretionaryIncrement() {
        return discretionary.getIncrement();
    }

    /**
     * Gets the discretionary start.
     *
     * @return The discretionary start
     */
    @SuppressWarnings("unused")
    public double getDiscretionaryStart() {
        return discretionary.getStart();
    }

    /**
     * Gets the fixed end.
     *
     * @return The fixed end
     */
    @SuppressWarnings("unused")
    public double getFixedEnd() {
        return fixed.getEnd();
    }

    /**
     * Gets the fixed increment.
     *
     * @return The fixed increment
     */
    @SuppressWarnings("unused")
    public double getFixedIncrement() {
        return fixed.getIncrement();
    }

    /**
     * Gets the fixed start.
     *
     * @return The fixed start
     */
    @SuppressWarnings("unused")
    public double getFixedStart() {
        return fixed.getStart();
    }

    /**
     * Gets the high end.
     *
     * @return The high end
     */
    @SuppressWarnings("unused")
    public double getHighEnd() {
        return high.getEnd();
    }

    /**
     * Gets the high increment.
     *
     * @return The high increment
     */
    @SuppressWarnings("unused")
    public double getHighIncrement() {
        return high.getIncrement();
    }

    /**
     * Gets the high start.
     *
     * @return The high start
     */
    @SuppressWarnings("unused")
    public double getHighStart() {
        return high.getStart();
    }

    /**
     * Gets the number of nested iterations for a parameter.
     *
     * @param parameter The parameter
     * @return The number of nested iterations for the parameter
     */
    private int getNestedIterations(@NotNull Parameter parameter) {

        /*
         * Get the ordinal of the parameter. Return the element from the
         * iterations array at one less than the ordinal if the ordinal is not
         * zero. Otherwise, return one.
         */
        final int ordinal = parameter.ordinal();
        return (0 < ordinal) ? iterations.get(ordinal - 1) : 1;
    }

    /**
     * Gets the observation window for acceptable terminal portfolio value.
     *
     * @return The observation window for acceptable terminal portfolio value
     */
    public @NotNull Pair<Double, Double> getObservationWindow() {
        return observationWindow;
    }

    /**
     * Gets the number of desired simulations.
     *
     * @return The number of desired simulations
     */
    public int getSimulationCount() {
        return simulationCount;
    }

    /**
     * Gets the zero end.
     *
     * @return The zero end
     */
    @SuppressWarnings("unused")
    public double getZeroEnd() {
        return zero.getEnd();
    }

    /**
     * Gets the zero increment.
     *
     * @return The zero increment
     */
    @SuppressWarnings("unused")
    public double getZeroIncrement() {
        return zero.getIncrement();
    }

    /**
     * Gets the zero start.
     *
     * @return The zero start
     */
    @SuppressWarnings("unused")
    public double getZeroStart() {
        return zero.getStart();
    }

    /**
     * Initializes the iterations list.
     */
    private void initialize() {

        // Clear the existing list, and cycle for each parameter.
        iterations.clear();
        final int length = Parameter.values().length;
        for (int i = 0; i < length; ++i) {

            /*
             * Add a default number of iterations to the list for the
             * first/next parameter.
             */
            iterations.add(defaultIterations);
        }
    }

    /**
     * Invalidates all increments.
     */
    @SuppressWarnings("unused")
    public void invalidateIncrements() {

        // Invalidate the increment in each start-end-increment.
        advance.setIncrement(invalidIncrement);
        bear.setIncrement(invalidIncrement);
        decline.setIncrement(invalidIncrement);
        discretionary.setIncrement(invalidIncrement);
        fixed.setIncrement(invalidIncrement);
        high.setIncrement(invalidIncrement);
        zero.setIncrement(invalidIncrement);
    }

    /**
     * Runs the simulations.
     *
     * @param writer A writer to receive output
     * @throws IOException Indicates an error when writing to the output file
     */
    private void run(@NotNull FileWriter writer) throws IOException {

        // Declare and initialize the checkpoint output format.
        final String checkpointFormat = "Iteration %d; Time: %s seconds; %d " +
                "recordable simulations%n";

        /*
         * Construct the repeating pattern of our csv file format with a
         * suitable precision. Next construct the csv file format itself.
         */
        final String repeatingPortion = String.format("%%.%df,", 5);
        final String csvFormat = String.format("%s%%s,%%s\n",
                repeatingPortion.repeat(iterations.size()));

        // Declare other local constants.
        final int reportThreshold = 20000;
        final int simulationCount = getSimulationCount();

        // Declare local variables.
        int increment;
        Date now;

        // Calculate the required number of recordable simulations.
        final int requiredRecordable = (simulationCount < 0) ?
                iterations.get(iterations.size() - 1) : simulationCount;

        // Cycle for the required number of recordable simulations.
        int current, iteration = 0;
        for (current = 0; current < requiredRecordable;
             current += increment, ++iteration) {

            // Set the decline threshold.
            simulation.setDeclineThreshold(selectValue(iteration,
                    Parameter.DECLINE, decline));

            // Set the advance threshold.
            simulation.setAdvanceThreshold(selectValue(iteration,
                    Parameter.ADVANCE, advance));

            // Set the zero market value.
            simulation.setZero(selectValue(iteration,
                    Parameter.ZERO, zero));

            // Set the bear market value.
            simulation.setBear(selectValue(iteration,
                    Parameter.BEAR, bear));

            // Set the high market value.
            simulation.setHigh(selectValue(iteration,
                    Parameter.HIGH, high));

            // Set the fixed withdrawal.
            simulation.setFixed(selectValue(iteration,
                    Parameter.FIXED, fixed));

            // Set the discretionary withdrawal.
            simulation.setDiscretionary(selectValue(iteration,
                    Parameter.DISCRETIONARY, discretionary));

            /*
             * Run the simulation, and receive the number of simulations to
             * increment. Are we at the report threshold with the current
             * iteration?
             */
            increment = runAndWrite(simulation, writer, csvFormat,
                    getObservationWindow());
            if (0 == (iteration % reportThreshold)) {

                /*
                 * We are at the report threshold with the current iteration.
                 * Write a checkpoint message.
                 */
                now = new Date();
                System.out.printf(checkpointFormat, iteration,
                        twoDigitFormat.format((now.getTime() - start.getTime())
                                / 1000.), current);
            }
        }

        // Write one last checkpoint message.
        now = new Date();
        System.out.printf(checkpointFormat, iteration,
                twoDigitFormat.format((now.getTime() - start.getTime()) /
                        1000.), current);
    }

    /**
     * Runs the simulations.
     *
     * @param output A file to receive the output
     */
    public void run(@NotNull File output) {

        // Set the iterations list. Try to create a file writer.
        setIterations();
        try (final FileWriter writer = new FileWriter(output)) {

            // Write the header.
            writer.write("discretionary,fixed,high,bear,zero,advance," +
                    "decline,value,draw\n");

            // Get the start time, and run the simulation with the file writer.
            start = new Date();
            run(writer);
        }

        /*
         * Catch any I/O exception that may occur, and output a message to
         * system error.
         */ catch (IOException exception) {
            System.err.printf("A writer for the simulations could not be " +
                    "created; message is: '%s'.", exception.getMessage());
        }
    }

    @Override
    public void run() {
        run(new File(ioDirectory, defaultOutput));
    }

    /**
     * Selects a value.
     *
     * @param iteration The iteration
     * @param parameter The parameter for which to select a value
     * @param sei       A relevant start-end-increment
     * @return The selected value
     */
    private double selectValue(int iteration, @NotNull Parameter parameter,
                               @NotNull StartEndIncrement sei) {

        // Get the increment and the start from the start-end-increment.
        final double increment = sei.getIncrement();
        final double start = sei.getStart();

        // Calculate the addend.
        //noinspection IntegerDivisionInFloatingPointContext
        final double addend = incrementOkay(increment) ?
                increment * ((iteration / getNestedIterations(parameter)) %
                        iterations.get(parameter.ordinal())) :
                generator.nextDouble() * (sei.getEnd() - start);

        // Return the selected value.
        return start + addend;
    }

    /**
     * Sets the advance end.
     *
     * @param end The advance end
     */
    @SuppressWarnings("unused")
    public void setAdvanceEnd(double end) {
        advance.setEnd(end);
    }

    /**
     * Sets the advance increment.
     *
     * @param increment The advance increment
     */
    @SuppressWarnings("unused")
    public void setAdvanceIncrement(double increment) {
        advance.setIncrement(increment);
    }

    /**
     * Sets the advance start.
     *
     * @param start The advance start
     */
    @SuppressWarnings("unused")
    public void setAdvanceStart(double start) {
        advance.setStart(start);
    }

    /**
     * Sets the bear end.
     *
     * @param end The bear end
     */
    @SuppressWarnings("unused")
    public void setBearEnd(double end) {
        bear.setEnd(end);
    }

    /**
     * Sets the bear increment.
     *
     * @param increment The bear increment
     */
    @SuppressWarnings("unused")
    public void setBearIncrement(double increment) {
        bear.setIncrement(increment);
    }

    /**
     * Sets the bear start.
     *
     * @param start The bear start
     */
    @SuppressWarnings("unused")
    public void setBearStart(double start) {
        bear.setStart(start);
    }

    /**
     * Sets the decline end.
     *
     * @param end The decline end
     */
    @SuppressWarnings("unused")
    public void setDeclineEnd(double end) {
        decline.setEnd(end);
    }

    /**
     * Sets the decline increment.
     *
     * @param increment The decline increment
     */
    @SuppressWarnings("unused")
    public void setDeclineIncrement(double increment) {
        decline.setIncrement(increment);
    }

    /**
     * Sets the decline start.
     *
     * @param start The decline start
     */
    @SuppressWarnings("unused")
    public void setDeclineStart(double start) {
        decline.setStart(start);
    }

    /**
     * Sets the discretionary end.
     *
     * @param end The discretionary end
     */
    @SuppressWarnings("unused")
    public void setDiscretionaryEnd(double end) {
        discretionary.setEnd(end);
    }

    /**
     * Sets the discretionary increment.
     *
     * @param increment The discretionary increment
     */
    @SuppressWarnings("unused")
    public void setDiscretionaryIncrement(double increment) {
        discretionary.setIncrement(increment);
    }

    /**
     * Sets the discretionary start.
     *
     * @param start The discretionary start
     */
    @SuppressWarnings("unused")
    public void setDiscretionaryStart(double start) {
        discretionary.setStart(start);
    }

    /**
     * Sets the end to start for each start-end-increment.
     */
    public void setEndToStart() {

        // Set the end to start for each start-end-increment.
        setEndToStart(advance);
        setEndToStart(bear);
        setEndToStart(decline);
        setEndToStart(discretionary);
        setEndToStart(fixed);
        setEndToStart(high);
        setEndToStart(zero);
    }

    /**
     * Sets the discretionary end.
     *
     * @param end The discretionary end
     */
    @SuppressWarnings("unused")
    public void setFixedEnd(double end) {
        fixed.setEnd(end);
    }

    /**
     * Sets the fixed increment.
     *
     * @param increment The fixed increment
     */
    @SuppressWarnings("unused")
    public void setFixedIncrement(double increment) {
        fixed.setIncrement(increment);
    }

    /**
     * Sets the fixed start.
     *
     * @param start The fixed start
     */
    @SuppressWarnings("unused")
    public void setFixedStart(double start) {
        fixed.setStart(start);
    }

    /**
     * Sets the high end.
     *
     * @param end The high end
     */
    @SuppressWarnings("unused")
    public void setHighEnd(double end) {
        high.setEnd(end);
    }

    /**
     * Sets the high increment.
     *
     * @param increment The high increment
     */
    @SuppressWarnings("unused")
    public void setHighIncrement(double increment) {
        high.setIncrement(increment);
    }

    /**
     * Sets the high start.
     *
     * @param start The high start
     */
    @SuppressWarnings("unused")
    public void setHighStart(double start) {
        high.setStart(start);
    }

    /**
     * Sets the iterations list.
     */
    private void setIterations() {

        /*
         * Initialize the iterations list, then set iterations for each
         * parameter.
         */
        initialize();
        setIterations(Parameter.DECLINE.ordinal(), decline);
        setIterations(Parameter.ADVANCE.ordinal(), advance);
        setIterations(Parameter.ZERO.ordinal(), zero);
        setIterations(Parameter.BEAR.ordinal(), bear);
        setIterations(Parameter.HIGH.ordinal(), high);
        setIterations(Parameter.FIXED.ordinal(), fixed);
        setIterations(Parameter.DISCRETIONARY.ordinal(), discretionary);
    }

    /**
     * Sets the value of the iterations list at a specific value given a
     * start-end-increment.
     *
     * @param index The index to use
     * @param sei   The start-end-increment
     */
    private void setIterations(int index, @NotNull StartEndIncrement sei) {

        /*
         * Get the value at the previous index in the iterations list. Use a
         * default if the index is zero or less. Get the increment from the
         * start-end-increment.
         */
        final int previous = (0 < index) ? iterations.get(index - 1) :
                defaultIterations;
        final double increment = sei.getIncrement();

        /*
         * Set the value at the indicated index in the iterations. Use a
         * default number of iterations if the increment is not okay.
         */
        iterations.set(index, previous * (incrementOkay(increment) ?
                (int) ((sei.getEnd() - sei.getStart()) / increment + 1) :
                defaultIterations));
    }

    /**
     * Sets the observation window for acceptable terminal portfolio value.
     *
     * @param observationWindow The observation window for acceptable terminal
     *                          portfolio value
     */
    private void setObservationWindow(
            @NotNull Pair<Double, Double> observationWindow) {
        this.observationWindow = observationWindow;
    }

    /**
     * Sets the number of desired simulations.
     *
     * @param simulationCount The number of desired simulations
     */
    public void setSimulationCount(int simulationCount) {
        this.simulationCount = simulationCount;
    }

    /**
     * Sets the zero end.
     *
     * @param end The zero end
     */
    @SuppressWarnings("unused")
    public void setZeroEnd(double end) {
        zero.setEnd(end);
    }

    /**
     * Sets the zero increment.
     *
     * @param increment The zero increment
     */
    @SuppressWarnings("unused")
    public void setZeroIncrement(double increment) {
        zero.setIncrement(increment);
    }

    /**
     * Sets the zero start.
     *
     * @param start The zero start
     */
    @SuppressWarnings("unused")
    public void setZeroStart(double start) {
        zero.setStart(start);
    }

    private enum Parameter {

        // The decline parameter
        DECLINE,

        // The advance parameter
        ADVANCE,

        // The zero parameter
        ZERO,

        // The bear parameter
        BEAR,

        // The high parameter
        HIGH,

        // The fixed parameter
        FIXED,

        // The discretionary parameter
        DISCRETIONARY
    }
}
