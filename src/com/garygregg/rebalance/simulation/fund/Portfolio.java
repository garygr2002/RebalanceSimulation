package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

public class Portfolio implements InterestAndPrice {

    // The initial price of all the funds
    private static final double initialPrice = 100.;

    // The initial value of all the funds
    private static final double initialValue = 0.;

    // The bond fund
    private final BondFund bondFund = new BondFund(initialValue, initialPrice);

    // The money fund
    private final MoneyFund moneyFund = new MoneyFund(initialValue);

    // The stock fund
    private final StockFund stockFund = new StockFund(initialValue,
            initialPrice);

    /*
     * The count of rebalances that occurred because there was too much of the
     * portfolio in stocks
     */
    private int rebalanceTooHigh;

    /*
     * The count of rebalances that occurred because there was not enough of
     * the portfolio in stocks
     */
    private int rebalanceTooLow;

    /**
     * Constructs the portfolio.
     */
    public Portfolio() {
        resetCounts();
    }

    /**
     * Tests whether its argument is a fraction.
     *
     * @param candidate The candidate fraction
     * @throws RuntimeException Indicates the argument is not a fraction
     */
    public static void testFraction(double candidate) {

        // Throw the exception if the argument is not a fraction.
        if ((candidate < -1.) || (1. < candidate)) {
            throw new RuntimeException(String.format("%f is not a fraction",
                    candidate));
        }
    }

    @Override
    public void addInterest(@NotNull MonthlyData data) {

        // Pass the command to all the funds.
        bondFund.addInterest(data);
        moneyFund.addInterest(data);
        stockFund.addInterest(data);
    }

    @Override
    public double addValue(double commandedAddend) {

        // Try to add the addend to the money fund.
        final double threshold = 0.;
        double totalAddend = moneyFund.addValue(commandedAddend);

        /*
         * Is there a difference between the commanded addend and the total
         * addend?
         */
        double difference = commandedAddend - totalAddend;
        if (threshold != difference) {

            /*
             * There is a difference between the commanded addend and the total
             * addend. Try to add the difference to the bond fund.
             */
            totalAddend += bondFund.addValue(difference);
            difference = commandedAddend - totalAddend;
        }

        // Add any remaining difference to the stock fund.
        if (threshold != difference) {
            totalAddend += stockFund.addValue(difference);
        }

        // Return the total addend.
        return totalAddend;
    }

    @Override
    public void adjustPrice(@NotNull MonthlyData data) {

        // Pass the command to all the funds.
        bondFund.adjustPrice(data);
        moneyFund.adjustPrice(data);
        stockFund.adjustPrice(data);
    }

    /**
     * Gets the count of rebalances that occurred because there was too much of
     * the portfolio in stocks.
     *
     * @return The count of rebalances that occurred because there was too much
     * of the portfolio in stocks
     */
    public int getRebalanceTooHigh() {
        return rebalanceTooHigh;
    }

    /**
     * Gets the count of rebalances that occurred because there was not enough
     * of the portfolio in stocks.
     *
     * @return The count of rebalances that occurred because there was not
     * enough of the portfolio in stocks
     */
    public int getRebalanceTooLow() {
        return rebalanceTooLow;
    }

    @Override
    public double getValue() {
        return bondFund.getValue() + moneyFund.getValue() +
                stockFund.getValue();
    }

    /**
     * Adjusts the portfolio in a standardized way for a monthly increment.
     *
     * @param data                  The data month
     * @param addend                The amount to add (or subtract) from the
     *                              portfolio at the end of the month
     * @param advanceThreshold      The advance threshold that triggers the
     *                              rebalance
     * @param declineThreshold      The decline threshold that triggers the
     *                              rebalance
     * @param fractionStock         The fraction of the portfolio to be
     *                              allocated to stock
     * @param remainderFractionBond The fraction of the non-stock portion of
     *                              the portfolio to be allocated to bonds
     */
    public void incrementMonth(@NotNull MonthlyData data,
                               double addend, double advanceThreshold,
                               double declineThreshold, double fractionStock,
                               double remainderFractionBond) {

        // Adjust price and add interest.
        adjustPrice(data);
        addInterest(data);

        // Add (or subtract) value, and rebalance as necessary.
        addValue(addend);
        rebalance(advanceThreshold, declineThreshold, fractionStock,
                remainderFractionBond);
    }

    /**
     * Rebalances the portfolio.
     *
     * @param advanceThreshold      The advance threshold that triggers the
     *                              rebalance
     * @param declineThreshold      The decline threshold that triggers the
     *                              rebalance
     * @param fractionStock         The fraction of the portfolio to be
     *                              allocated to stock
     * @param remainderFractionBond The fraction of the non-stock portion of
     *                              the portfolio to be allocated to bonds
     */
    public void rebalance(double advanceThreshold, double declineThreshold,
                          double fractionStock, double remainderFractionBond) {

        // Tests all arguments to make sure they are fractions.
        testFraction(declineThreshold);
        testFraction(fractionStock);
        testFraction(remainderFractionBond);

        /*
         * Get the value of the stock fund, and calculate the total value of
         * the portfolio.
         */
        final double stockValue = stockFund.getValue();
        double value = getValue();

        /*
         * Determine how much deviation there is between the current fraction
         * of the portfolio allocated to stock, and the desired fraction. Is
         * the deviation at, or below the decline threshold?
         */
        final double deviation = stockValue / value - fractionStock;
        boolean rebalance = (deviation <= declineThreshold);
        if (rebalance) {

            /*
             * The deviation is at, or below the decline threshold. Increment
             * the count of rebalances made because the deviation was too low.
             */
            ++rebalanceTooLow;
        }

        // Is the deviation at, or above the advance threshold?
        else //noinspection AssignmentUsedAsCondition
            if ((rebalance = (advanceThreshold <= deviation))) {

                /*
                 * The deviation is at, or above the advance threshold.
                 * Increment the count of rebalances made because the deviation
                 * was too high.
                 */
                ++rebalanceTooHigh;
            }

        // Should we rebalance?
        if (rebalance) {

            /*
             * We should rebalance. Allocate the proper fraction of the
             * portfolio to stock. Subtract the stock value from the value of
             * the fund.
             */
            stockFund.setValue(value * fractionStock);
            value -= stockFund.getValue();

            // Reallocate the bond fund and money fund balances.
            bondFund.setValue(value * remainderFractionBond);
            moneyFund.setValue(value - bondFund.getValue());
        }
    }

    /**
     * Resets the rebalance counters.
     */
    public void resetCounts() {

        // Reset both counters.
        rebalanceTooHigh = 0;
        rebalanceTooLow = 0;
    }

    @Override
    public void setValue(double value) {

        // Set the bond fund.
        final double defaultSet = 0.;
        bondFund.setValue(defaultSet);

        /*
         * Set the money fund and the stock fund. Only the money fund gets the
         * given value.
         */
        moneyFund.setValue(value);
        stockFund.setValue(defaultSet);
    }
}
