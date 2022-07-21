package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

/**
 * Price is fixed for this fund. Only the treasury bill interest rate need be
 * set in order to add interest.
 */
public class MoneyFund extends Fund {

    /**
     * Constructs the money fund.
     *
     * @param value The value of the money fund
     */
    public MoneyFund(double value) {
        super(value, 1.);
    }

    @Override
    public void addInterest(@NotNull MonthlyData data) {

        /*
         * Calculate the interest rate as a fraction of the treasury bill
         * interest rate. Is the interest rate greater than one?
         */
        final double interestRate = data.getTreasuryBill() * 0.999466;
        if (1. < interestRate) {

            /*
             * The interest rate is greater than one. Reset the value of the
             * money fund.
             */
            setValue(getValue() * interestRate);
        }
    }
}
