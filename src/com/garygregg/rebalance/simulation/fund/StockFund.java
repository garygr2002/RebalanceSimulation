package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

/**
 * For the price of this fund to be accurate, the S&P 500 valuation at the
 * start of the month should equal to that for the end of the previous month.
 * Only the S&P 500 dividend end need be set in order to add interest.
 */
public class StockFund extends Fund {

    /**
     * Constructs the stock fund.
     *
     * @param value The value of the stock fund
     */
    public StockFund(double value, double price) {
        super(value, price);
    }

    @Override
    public void addInterest(@NotNull MonthlyData data) {
        setValue(getValue() * data.getSAndP500DividendEnd());
    }

    @Override
    public void adjustPrice(@NotNull MonthlyData data) {

        // Set the price, then check it.
        setPrice(getPrice() * data.getSAndP500ValueEnd() /
                data.getSAndP500ValueStart());
        checkPrice();
    }
}
