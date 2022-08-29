package com.garygregg.rebalance.simulation.simulation;

import com.garygregg.rebalance.simulation.data.Database;
import com.garygregg.rebalance.simulation.data.MonthlyData;
import com.garygregg.rebalance.simulation.fund.Portfolio;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

    /*
     * The remainder of a portfolio not allocated to stocks that is allocated
     * to bonds
     */
    private static final double remainderFractionBond = 2. / 3.;

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

    // The discretionary annual percent of the portfolio for addition
    private double discretionary = -0.04 /
            Database.getMonthCount();

    // The discretionary draw portion
    private double discretionaryPortion;

    // The end date for the simulation
    private Date end;

    // The fixed annual percent of the portfolio for addition (with COLA)
    private double fixed = -0.04 / Database.getMonthCount();

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
     * Gets the initial portfolio value.
     *
     * @return The initial portfolio value
     */
    public static double getInitialValue() {
        return Math.pow(10., 6);
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
     * Gets the discretionary annual percent of the portfolio for addition.
     *
     * @return The discretionary annual percent of the portfolio
     * for addition
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
     * Gets the fixed annual percent of the portfolio for addition (with COLA).
     *
     * @return The fixed annual percent of the portfolio for addition
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
    public double run(@SuppressWarnings("SameParameterValue") boolean noisy) {

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
        portfolio.setValue(getInitialValue());
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
            portfolio.incrementMonth(data, draw, getAdvanceThreshold(),
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
     * Sets the discretionary annual percent of the portfolio for addition.
     *
     * @param discretionary The discretionary annual percent of the portfolio
     *                      for addition
     */
    public void setDiscretionary(double discretionary) {
        this.discretionary = discretionary;
    }

    /**
     * Sets the fixed annual percent of the portfolio for addition (with COLA).
     *
     * @param fixed The fixed annual percent of the portfolio for addition
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
