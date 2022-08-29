package com.garygregg.rebalance.simulation.simulation;

class StartEndIncrement {

    // The initial end
    private final double initialEnd;

    // The initial increment
    private final double initialIncrement;

    // The initial start
    private final double initialStart;

    // The end
    private double end;

    // The increment
    private double increment;

    // The start
    private double start;

    /**
     * Constructs the StartEndIncrement.
     *
     * @param initialStart     The initial start
     * @param initialEnd       The initial end
     * @param initialIncrement The initial increment
     */
    public StartEndIncrement(double initialStart, double initialEnd,
                             double initialIncrement) {

        // Set all the initial constants, and reset the member variables.
        this.initialStart = initialStart;
        this.initialEnd = initialEnd;
        this.initialIncrement = initialIncrement;
        resetAll();
    }

    /**
     * Gets the end.
     *
     * @return The end
     */
    public double getEnd() {
        return end;
    }

    /**
     * Gets the increment.
     *
     * @return The increment
     */
    public double getIncrement() {
        return increment;
    }

    /**
     * Gets the start.
     *
     * @return The start
     */
    public double getStart() {
        return start;
    }

    /**
     * Resets all the member variables.
     */
    public void resetAll() {

        // Call all the resets.
        resetEnd();
        resetIncrement();
        resetStart();
    }

    /**
     * Resets the end.
     */
    public void resetEnd() {
        setEnd(initialEnd);
    }

    /**
     * Resets the increment.
     */
    public void resetIncrement() {
        setIncrement(initialIncrement);
    }

    /**
     * Resets the start.
     */
    public void resetStart() {
        setStart(initialStart);
    }

    /**
     * Sets the end.
     *
     * @param end The end
     */
    public void setEnd(double end) {
        this.end = end;
    }

    /**
     * Sets the increment.
     *
     * @param increment The increment
     */
    public void setIncrement(double increment) {
        this.increment = increment;
    }

    /**
     * Sets the start.
     *
     * @param start The start
     */
    public void setStart(double start) {
        this.start = start;
    }
}
