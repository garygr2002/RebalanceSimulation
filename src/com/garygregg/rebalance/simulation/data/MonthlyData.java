package com.garygregg.rebalance.simulation.data;

public class MonthlyData {

    // The federal funds rate
    private Double fedFunds;

    // The annual rate of inflation
    private Double inflation;

    // The dividend yield of the S&P 500 at the end of the month
    private Double sAndP500DividendEnd;

    // The dividend yield of the S&P 500 at the start of the month
    private Double sAndP500DividendStart;

    // The value of the S&P 500 at the end of the month
    private Double sAndP500ValueEnd;

    // The value of the S&P 500 at the start of the month
    private Double sAndP500ValueStart;

    // The rate of interest on a treasury bill
    private Double treasuryBill;

    /*
     * The rate of interest on a 10-year treasury bond at the end of the
     * month
     */
    private Double treasuryRateEnd;

    /*
     * The rate of interest on a 10-year treasury bond at the start of the
     * month
     */
    private Double treasuryRateStart;

    /**
     * Gets the federal funds rate.
     *
     * @return The federal funds rate
     */
    public Double getFedFunds() {
        return fedFunds;
    }

    /**
     * Gets the annual rate of inflation.
     *
     * @return The annual rate of inflation
     */
    public Double getInflation() {
        return inflation;
    }

    /**
     * Gets the rate of interest on a treasury bill.
     *
     * @return The rate of interest on a treasury bill
     */
    public Double getTreasuryBill() {
        return treasuryBill;
    }

    /**
     * Gets the rate of interest on a 10-year treasury bond at the end of the
     * month.
     *
     * @return The rate of interest on a 10-year treasury bond at the end of
     * the month
     */
    public Double getTreasuryRateEnd() {
        return treasuryRateEnd;
    }

    /**
     * Gets the rate of interest on a 10-year treasury bond at the start of the
     * month.
     *
     * @return The rate of interest on a 10-year treasury bond at the start of
     * the month
     */
    public Double getTreasuryRateStart() {
        return treasuryRateStart;
    }

    /**
     * Get the dividend yield of the S&P 500 at the end of the month.
     *
     * @return The dividend yield of the S&P 500 at the end of the month
     */
    public Double getSAndP500DividendEnd() {
        return sAndP500DividendEnd;
    }

    /**
     * Get the dividend yield of the S&P 500 at the start of the month.
     *
     * @return The dividend yield of the S&P 500 at the start of the month
     */
    public Double getSAndP500DividendStart() {
        return sAndP500DividendStart;
    }

    /**
     * Gets the value of the S&P 500 at the end of the month.
     *
     * @return The value of the S&P 500 at the end of the month
     */
    public Double getSAndP500ValueEnd() {
        return sAndP500ValueEnd;
    }

    /**
     * Gets the value of the S&P 500 at the start of the month.
     *
     * @return The value of the S&P 500 at the start of the month
     */
    public Double getSAndP500ValueStart() {
        return sAndP500ValueStart;
    }

    /**
     * Sets the federal funds rate.
     *
     * @param fedFunds The federal funds rate
     */
    void setFedFunds(Double fedFunds) {
        this.fedFunds = fedFunds;
    }

    /**
     * Sets the annual rate of inflation.
     *
     * @param inflation The annual rate of inflation
     */
    void setInflation(double inflation) {
        this.inflation = inflation;
    }

    /**
     * Sets the rate of interest on a treasury bill.
     *
     * @param treasuryBill The rate of interest on a treasury bill
     */
    void setTreasuryBill(double treasuryBill) {
        this.treasuryBill = treasuryBill;
    }

    /**
     * Sets the rate of interest on a 10-year treasury bond at the end of the
     * month.
     *
     * @param treasuryRateEnd The rate of interest on a 10-year treasury bond
     *                        at the end of the month
     */
    void setTreasuryRateEnd(Double treasuryRateEnd) {
        this.treasuryRateEnd = treasuryRateEnd;
    }

    /**
     * Sets the rate of interest on a 10-year treasury bond at the start of the
     * month.
     *
     * @param treasuryRateStart The rate of interest on a 10-year treasury bond
     *                          at the start of the month
     */
    void setTreasuryRateStart(double treasuryRateStart) {
        this.treasuryRateStart = treasuryRateStart;
    }

    /**
     * Set the dividend yield of the S&P 500 at the end of the month.
     *
     * @param sAndP500DividendEnd The dividend yield of the S&P 500 at the
     *                            end of the month
     */
    void setSAndP500DividendEnd(Double sAndP500DividendEnd) {
        this.sAndP500DividendEnd = sAndP500DividendEnd;
    }

    /**
     * Set the dividend yield of the S&P 500 at the start of the month.
     *
     * @param sAndP500DividendStart The dividend yield of the S&P 500 at the
     *                              start of the month
     */
    void setSAndP500DividendStart(Double sAndP500DividendStart) {
        this.sAndP500DividendStart = sAndP500DividendStart;
    }

    /**
     * Sets the value of the S&P 500 at the end of the month.
     *
     * @param sAndP500ValueEnd The value of the S&P 500 at the end of the month
     */
    void setSAndP500ValueEnd(double sAndP500ValueEnd) {
        this.sAndP500ValueEnd = sAndP500ValueEnd;
    }

    /**
     * Sets the value of the S&P 500 at the start of the month.
     *
     * @param sAndP500ValueStart The value of the S&P 500 at the start of the
     *                           month
     */
    void setSAndP500ValueStart(double sAndP500ValueStart) {
        this.sAndP500ValueStart = sAndP500ValueStart;
    }
}
