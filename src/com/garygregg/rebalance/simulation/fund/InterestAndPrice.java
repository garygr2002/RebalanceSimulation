package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

public interface InterestAndPrice {

    /**
     * Adds interest to the fund.
     *
     * @param data The monthly data containing interest rate information
     */
    void addInterest(@NotNull MonthlyData data);

    /**
     * Adds (or subtracts) value if the operation does not cause positive value
     * to become negative.
     *
     * @param addend The value to add (or subtract)
     * @return The amount added (or subtracted)
     */
    double addValue(double addend);

    /**
     * Adjusts the price of the fund.
     *
     * @param data The monthly data containing pricing information
     */
    void adjustPrice(@NotNull MonthlyData data);

    /**
     * Gets the value of the holding.
     *
     * @return The value of the holding
     */
    double getValue();

    /**
     * Sets the value of the holding.
     *
     * @param value The value of the holding
     */
    void setValue(double value);
}
