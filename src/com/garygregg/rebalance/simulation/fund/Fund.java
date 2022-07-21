package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

public abstract class Fund implements InterestAndPrice {

    // The price of one share
    private double price;

    // The number of shares
    private double shares;

    // The value of the holding
    private double value;

    /**
     * Constructs the fund.
     *
     * @param value The value of the fund
     * @param price The price of one share
     */
    public Fund(double value, double price) {

        /*
         * Set the price and value of the fund, and calculate the number of
         * shares.
         */
        this.price = price;
        this.value = value;
        shares = value / price;
    }

    @Override
    public double addValue(double addend) {

        // Get the existing value.
        final double threshold = 0.;
        final double existingValue = getValue();

        /*
         * Calculate the proposed value. Ensure any non-negative existing value
         * does not transition to negative. Would this transition take place?
         */
        double proposedValue = existingValue + addend;
        if ((threshold <= existingValue) && (proposedValue < threshold)) {

            /*
             * The transition would take place. Modify the addend so that the
             * proposed value would become zero.
             */
            addend = -existingValue;
        }

        // Set the value, and return the (possibly modified) addend.
        setValue(existingValue + addend);
        return addend;
    }

    @Override
    public void adjustPrice(@NotNull MonthlyData data) {

        // The default is to do nothing, which is the behavior of a money fund.
    }

    /**
     * Checks the price; performs a reverse split of the price is too low, and
     * a split if the price is too high.
     */
    protected void checkPrice() {

        // Get the current price. Is the price too low?
        final double price = getPrice();
        final double split = 2.;
        if (price < 10.) {

            // The price is too low. Perform a reverse split.
            setPrice(price * split);
            setShares(getShares() / split);
        }

        // Is the price too high?
        if (200. < price) {

            // The price is too high. Perform a split.
            setPrice(price / split);
            setShares(getShares() * split);
        }
    }

    /**
     * Gets the price of one share.
     *
     * @return The price of one share
     */
    public double getPrice() {
        return price;
    }

    /**
     * Gets the number of shares.
     *
     * @return The number of shares
     */
    public double getShares() {
        return shares;
    }

    @Override
    public double getValue() {
        return value;
    }

    /**
     * Sets the price of one share.
     *
     * @param price The price of one share
     */
    protected void setPrice(double price) {

        // Set the price, and recalculate the value.
        this.price = price;
        value = getPrice() * getShares();
    }

    /**
     * Sets the number of shares.
     *
     * @param shares The number of shares
     */
    protected void setShares(double shares) {

        // Set the number of shares, and recalculate the value.
        this.shares = shares;
        value = getPrice() * getShares();
    }

    @Override
    public void setValue(double value) {

        // Set the value of the holding, and recalculate the number of shares.
        this.value = value;
        setShares(getValue() / getPrice());
    }
}
