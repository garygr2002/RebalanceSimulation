package com.garygregg.rebalance.simulation.fund;

import com.garygregg.rebalance.simulation.data.MonthlyData;
import org.jetbrains.annotations.NotNull;

/**
 * For the price of this fund to be accurate, the treasury rate at the start of
 * any month should equal to that for the end of the previous month. Only the
 * treasury rate end need be set in order to add interest.
 */
public class BondFund extends Fund {

    // The number of months in ten years
    private static final int monthsInTenYears = 12 * 10;

    /*
     * The portion of the price of the bond fund that is insensitive to
     * interest rates because new bonds are being acquired
     */
    private static final double insensitivePortion = 1. / monthsInTenYears;

    /*
     * The portion of the price of the bond fund that is sensitive to interest
     * rates because of existing bonds in the portfolio
     */
    private static final double sensitivePortion = 1. - insensitivePortion;

    /**
     * Constructs the bond fund.
     *
     * @param value The value of the bond fund
     */
    public BondFund(double value, double price) {
        super(value, price);
    }

    @Override
    public void addInterest(@NotNull MonthlyData data) {
        setValue(getValue() * data.getTreasuryRateEnd());
    }

    @Override
    public void adjustPrice(@NotNull MonthlyData data) {

        /*
         * Reset the price using the starting and ending interest rates, taking
         * into account the interest-rate insensitive and interest-rate
         * sensitive portions of the portfolio. Check the price.
         */
        setPrice(getPrice() * (sensitivePortion * data.getTreasuryRateEnd() /
                data.getTreasuryRateStart() + insensitivePortion));
        checkPrice();
    }
}
