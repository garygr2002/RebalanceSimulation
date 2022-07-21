package com.garygregg.rebalance.simulation.data;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@SuppressWarnings("GrazieInspection")
public class Database {

    // A calendar instance
    private static final Calendar calendar = Calendar.getInstance();

    /*
     * The date format used for government rates (fed funds, treasury bill and
     * treasury bond) and S&P 500 dividend
     */
    private static final SimpleDateFormat governmentAndDividend =
            new SimpleDateFormat("yyyy-MM-dd");

    // A database instance
    private static final Database instance = new Database();

    // The count of months
    private static final int monthCount = Calendar.DECEMBER + 1;

    // The date format used in the S&P 500 valuation file
    private static final SimpleDateFormat sAndPValuation =
            new SimpleDateFormat("MMM dd, yyyy");

    // The target day in the month
    private static final int targetDayInMonth = 1;

    // A file type to file object map
    private static final Map<FileType, File> typeToFileMap = new HashMap<>();

    // A number format for USA English
    private static final NumberFormat usaEnglishFormat =
            NumberFormat.getInstance(new Locale("en", "US"));

    // For use in reading data lines
    private static BufferedReader reader;

    static {

        // The well-known file containing alternate inflation
        typeToFileMap.put(FileType.ALTERNATE_INFLATION,
                constructFile("alternate-inflation.txt"));

        // The well-known file containing the fed funds rate
        typeToFileMap.put(FileType.FED_FUNDS,
                constructFile("fed-funds-rate-historical-chart.txt"));

        // The well-known file containing historical inflation
        typeToFileMap.put(FileType.HISTORICAL_INFLATION,
                constructFile("historical-inflation-rate-by-year.txt"));

        // The well-known file with treasury bill yields
        typeToFileMap.put(FileType.TREASURY_BILL,
                constructFile("treasury-bill-secondary.txt"));

        // The well-known file with S&P 500 dividend data
        typeToFileMap.put(FileType.S_AND_P_500_DIVIDEND,
                constructFile("MULTPL-SP500_DIV_YIELD_MONTH.txt"));

        // The well-known file with S&P 500 valuation data
        typeToFileMap.put(FileType.S_AND_P_500_VALUATION,
                constructFile("SandP500.txt"));

        // The well-known file with 10-year treasury bond rates
        typeToFileMap.put(FileType.TREASURY_BOND,
                constructFile("1-year-treasury-rate-yield-chart.txt"));
    }

    static {
        buildDatabase();
    }

    // Initialize the map of monthly data
    private final Map<Date, MonthlyData> map = new HashMap<>();

    /**
     * Builds the database instance.
     */
    private static void buildDatabase() {

        // Get the database instance. Try to do things.
        Database database = getInstance();
        try {

            // Try to set inflation.
            database.setInflation();
            closeReader();

            // Try to set federal funds.
            database.setFedFunds();
            closeReader();

            // Try to set the S&P 500 dividend.
            database.setSandP500Dividend();
            closeReader();

            // Try to set S&P 500 valuation.
            database.setSandP500Value();
            closeReader();

            // Try to set treasury bill.
            database.setTreasuryBill();
            closeReader();

            // Try to set 10-year treasury rate.
            database.setTreasuryRate();
            closeReader();
        }

        /*
         * Catch I/O exceptions, and number format exceptions. Print their
         * messages to the error stream.
         */ catch (IOException | NumberFormatException |
                   ParseException exception) {
            System.err.println(exception.getMessage());
        }

        // Check well-known ranges for full dates, and non-null entries.
        database.checkFullDates(1871, 2021);
        database.checkNonNullEntries(1962, 2021);
    }

    /**
     * Checks that all values within a monthly data object are non-null.
     *
     * @param data A monthly data object.
     */
    private static void checkNonNullEntries(@NotNull MonthlyData data) {

        // Check fed funds and inflation.
        checkNonNullValue(data.getFedFunds(), "Fed Funds");
        checkNonNullValue(data.getInflation(), "Inflation");

        // Check S&P 500 dividend start and end.
        checkNonNullValue(data.getSAndP500DividendStart(),
                "S&P 500 Dividend Start");
        checkNonNullValue(data.getSAndP500DividendEnd(),
                "S&P 500 Dividend End");

        // Check S&P 500 value start and end.
        checkNonNullValue(data.getSAndP500ValueStart(),
                "S&P 500 Value Start");
        checkNonNullValue(data.getSAndP500ValueEnd(),
                "S&P 500 Value End");

        // Check treasury bill, and treasury rate start and end.
        checkNonNullValue(data.getTreasuryBill(), "Treasury Bill");
        checkNonNullValue(data.getTreasuryRateStart(), "Treasury Rate Start");
        checkNonNullValue(data.getTreasuryRateEnd(), "Treasury Rate End");
    }

    /**
     * Checks that a value is non-null.
     *
     * @param value      A value
     * @param descriptor A descriptor for the value
     */
    private static void checkNonNullValue(Object value,
                                          @NotNull String descriptor) {

        // Print a message to system error if the value is null.
        if (null == value) {
            System.err.printf("%s data for %04d/%02d is null!\n", descriptor,
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH) + 1);
        }
    }

    /**
     * Closes the data lines reader, if not null, and sets it to null.
     *
     * @throws IOException Indicates an exception occurred while trying to
     *                     close the data lines reader
     */
    private static void closeReader() throws IOException {

        // Close the reader if it is not null.
        if (null != reader) {
            reader.close();
        }

        // Set the reader to null.
        reader = null;
    }

    /**
     * Constructs a file object with the known prefix, file separator, and a
     * given suffix.
     *
     * @param suffix The given suffix
     * @return A file object constructed as described
     */
    private static @NotNull File constructFile(@NotNull String suffix) {
        return new File("data", suffix);
    }

    /**
     * Gets a date given a year and month.
     *
     * @param year  The given year
     * @param month The given month
     * @return A date corresponding to the given year and month
     */
    public static @NotNull Date getDate(int year, int month) {

        /*
         * Clear the calendar before setting its fields. Return the time from
         * the calendar.
         */
        calendar.clear();
        calendar.set(year, month, getTargetDayInMonth(), 0, 0, 0);
        return calendar.getTime();
    }

    /**
     * Gets a file object given a file type.
     *
     * @param type The given file type
     * @return A file object, or null if none exists for the file type
     */
    private static File getFile(@NotNull FileType type) {
        return typeToFileMap.get(type);
    }

    /**
     * Gets an instance of the database.
     *
     * @return An instance of the database
     */
    public static Database getInstance() {
        return instance;
    }

    /**
     * Returns the count of months.
     *
     * @return The count of months
     */
    public static int getMonthCount() {
        return monthCount;
    }

    /**
     * Gets a reader for a given file type.
     *
     * @param type The given file type
     * @return A file reader for the given file type
     * @throws FileNotFoundException Indicates that the corresponding file does
     *                               not exist
     */
    private static @NotNull BufferedReader getReader(@NotNull FileType type)
            throws FileNotFoundException {
        return new BufferedReader(new FileReader(Database.getFile(type)));
    }

    /**
     * Gets the target day in the month.
     *
     * @return The target day in the month
     */
    private static int getTargetDayInMonth() {
        return targetDayInMonth;
    }

    /**
     * Tests this class.
     *
     * @param arguments Command line arguments
     */
    public static void main(@NotNull String[] arguments) {

        // Do things.
        Database database = getInstance();
        System.out.printf("I %shave a database instance!",
                (null == database) ? "do not " : "");
    }

    /**
     * Converts an annual percentage to monthly multiplicand; example: 10.0%
     * becomes 1.007974140.
     *
     * @param percent An annual percentage
     * @return A monthly multiplicand
     */
    public static double percentToMultiplicand(double percent) {
        return Math.exp(Math.log(percent / 100. + 1.) / getMonthCount());
    }

    /**
     * Checks the database such that every month between two inclusive years
     * has a database entry.
     *
     * @param startYear The start year
     * @param endYear   The end year
     */
    @SuppressWarnings("SameParameterValue")
    private void checkFullDates(int startYear, int endYear) {

        /*
         * Get the month count. Declare a variable to receive monthly data.
         * Cycle for each year, inclusive.
         */
        final int monthCount = getMonthCount();
        MonthlyData data;
        for (int year = startYear; year <= endYear; ++year) {

            // Cycle for each month in the first/next year.
            for (int month = 0; month < monthCount; ++month) {

                /*
                 * Get a data entry for the first/next month. Is the data entry
                 * null?
                 */
                data = getEntry(year, month);
                if (null == data) {

                    // The data entry is null. Print a message to system error.
                    System.err.printf("Entry for %04d/%02d is missing!%n",
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH) + 1);
                }
            }
        }
    }

    /**
     * Checks that there are entries for each month between two years,
     * inclusive.
     *
     * @param startYear The start year
     * @param endYear   The end year
     */
    @SuppressWarnings("SameParameterValue")
    private void checkNonNullEntries(int startYear, int endYear) {

        // Cycle for each year, inclusive.
        final int monthCount = getMonthCount();
        for (int year = startYear; year <= endYear; ++year) {

            /*
             * Cycle for each month in the first/next year. Check for non-null
             * entries.
             */
            for (int month = 0; month < monthCount; ++month) {
                checkNonNullEntries(getEntry(year, month));
            }
        }
    }

    /**
     * Gets an entry in the database that corresponds to the given year and
     * month.
     *
     * @param year  The year of the entry
     * @param month The month of the entry
     * @return An entry corresponding to the year and month, or null if no
     * such entry exists
     */
    public MonthlyData getEntry(int year, int month) {
        return map.get(getDate(year, month));
    }

    /**
     * Gets (or creates) an entry in the database that corresponds to the given
     * year and month.
     *
     * @param year  The year of the entry
     * @param month The month of the entry
     * @return An entry corresponding to the year and month
     */
    private @NotNull MonthlyData getOrCreateEntry(int year, int month) {

        /*
         * Get any existing monthly data for the given year and month. Is there
         * no existing monthly data corresponding to the year and month?
         */
        MonthlyData monthlyData = getEntry(year, month);
        if (null == monthlyData) {

            /*
             * There is no existing monthly data corresponding to the given
             * year and month. At this point the year and month have already
             * been set in the calendar, so another call to 'getTime()' will
             * retrieve the cached date value. Create a new monthly data entry,
             * cache it, and put the value in the database.
             */
            map.put(calendar.getTime(), monthlyData = new MonthlyData());
        }

        // Return the monthly data.
        return monthlyData;
    }

    /**
     * Sets the fed funds rate in the database.
     *
     * @throws IOException           Indicates an exception occurred while
     *                               trying to read a file line containing fed
     *                               funds data
     * @throws NumberFormatException Indicates an exception occurred while
     *                               attempting to parse a floating point value
     * @throws ParseException        Indicates a date could not be parsed
     */
    private void setFedFunds() throws IOException, NumberFormatException,
            ParseException {

        /*
         * Get a reader for the fed funds file, and read the first line. Is
         * there even one line?
         */
        reader = getReader(FileType.FED_FUNDS);
        String line = reader.readLine();
        if (null != line) {

            /*
             * Declare a constant indexing the last expected element in any
             * line, and a constant identifying the target day in any month.
             * Declare a variable to receive a monthly data object.
             */
            final int indexOfLastElement = 1;
            final int targetDayInMonth = getTargetDayInMonth();
            MonthlyData data;

            /*
             * Declare a variable to receive data items from the lines in the
             * file, and another to receive the number of said elements in any
             * line.
             */
            String[] elements;
            int elementsLength;

            // Read data lines while they exist.
            line = reader.readLine();
            while (null != line) {

                /*
                 * Split the elements around a comma. Get the number of
                 * distinct elements. Are there at least the expected number of
                 * data elements?
                 */
                elements = line.split("\\s*,\\s*");
                elementsLength = elements.length;
                if (indexOfLastElement < elementsLength) {

                    /*
                     * There are at least the expected number of data elements.
                     * Parse the first element - which is the date - and set it
                     * in the calendar. Is the day of the month the target day?
                     */
                    calendar.setTime(governmentAndDividend.parse(
                            elements[indexOfLastElement - 1]));
                    if (targetDayInMonth == calendar.get(Calendar.DAY_OF_MONTH)) {

                        /*
                         * The day of the month is the target day. Get, or
                         * create the monthly data for the indicated year and
                         * month.
                         */
                        data = getOrCreateEntry(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH));

                        /*
                         * Calculate the fed funds item for the indicated
                         * month, and add it to the database.
                         */
                        data.setFedFunds(percentToMultiplicand(
                                Double.parseDouble(elements[indexOfLastElement])));
                    }
                }

                // Read the next data line.
                line = reader.readLine();
            }
        }
    }

    /**
     * Sets the inflation rate in the database.
     *
     * @throws IOException           Indicates an exception occurred while
     *                               trying to read a file line containing
     *                               inflation data
     * @throws NumberFormatException Indicates an exception occurred while
     *                               attempting to parse an integral or
     *                               floating point value
     */
    private void setInflation() throws IOException, NumberFormatException {

        /*
         * Get a reader for the alternate inflation file, and read the first
         * line. Is there even one line?
         */
        reader = getReader(FileType.ALTERNATE_INFLATION);
        String line = reader.readLine();
        if (null != line) {

            /*
             * There is at least one line. We know the first line to be the
             * header. Get the number of months in a year. Declare a variable
             * to receive a year.
             */
            final double monthCount = getMonthCount();
            int year;

            /*
             * Declare a variable to receive data items from lines in the file,
             * and a variable to receive the number of elements in any
             * line.
             */
            String[] elements;
            int elementsLength;

            // Read the first data line. Cycle while data lines exist.
            line = reader.readLine();
            while (null != line) {

                /*
                 * Split the elements around whitespace. Get the number of
                 * distinct data elements. Is there at least one data element?
                 */
                elements = line.split("\\s+");
                elementsLength = elements.length;
                if (0 < elementsLength) {

                    /*
                     * There is at least one data element. The first data
                     * element is the year. Parse the year, and cycle for the
                     * remainder of the data items, which are inflation
                     * observations for each month in the year.
                     */
                    year = Integer.parseInt(elements[0]);
                    for (int month = 0, index = month + 1;
                         (month < monthCount) && (index < elementsLength);
                         ++month, ++index) {

                        /*
                         * Call 'getDate' for the side effect of setting the
                         * calendar. Calculate a monthly inflation item for the
                         * first/next month, and add it to the database.
                         */
                        getDate(year, month);
                        getOrCreateEntry(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)).setInflation(
                                percentToMultiplicand(
                                        Double.parseDouble(elements[index])));
                    }
                }

                // Read the next data line.
                line = reader.readLine();
            }
        }
    }

    /**
     * Sets the S&P 500 dividend yield in the database.
     *
     * @throws IOException           Indicates an exception occurred while
     *                               trying to read a file line containing S&P
     *                               500 dividend data
     * @throws NumberFormatException Indicates an exception occurred while
     *                               attempting to parse a floating point value
     * @throws ParseException        Indicates a date could not be parsed
     */
    private void setSandP500Dividend() throws IOException,
            NumberFormatException, ParseException {

        /*
         * Get a reader for the S&P 500 dividend file, and read the first
         * line. Is there even one line?
         */
        reader = getReader(FileType.S_AND_P_500_DIVIDEND);
        String line = reader.readLine();
        if (null != line) {

            /*
             * Declare a constant indexing the last expected element in any
             * line, and a constant identifying the target day in any month.
             */
            final int indexOfLastElement = 1;
            final int targetDayInMonth = getTargetDayInMonth();

            /*
             * Declare a variable to receive data items from the lines in the
             * file, and another to receive the number of said elements in any
             * line. Declare a variable to receive an S&P 500 dividend
             * multiplicand.
             */
            String[] elements;
            int elementsLength;
            double multiplicand;

            // Read the first data line. Cycle while data lines exist.
            line = reader.readLine();
            while (null != line) {

                /*
                 * Split the elements around a comma. Get the number of
                 * distinct elements. Are there at least the expected number of
                 * data elements?
                 */
                elements = line.split("\\s*,\\s*");
                elementsLength = elements.length;
                if (indexOfLastElement < elementsLength) {

                    /*
                     * There are at least the expected number of data elements.
                     * Parse the first element - which is the date - and set it
                     * in the calendar.
                     */
                    calendar.setTime(governmentAndDividend.parse(
                            elements[indexOfLastElement - 1]));

                    /*
                     * Is the day of the month something other than the target
                     * day?
                     */
                    if (targetDayInMonth != calendar.get(
                            Calendar.DAY_OF_MONTH)) {

                        /*
                         * The day of the month is something other than the
                         * target day. Calculate the multiplicand by parsing
                         * the annual yield, and converting it.
                         */
                        multiplicand = percentToMultiplicand(
                                Double.parseDouble(
                                        elements[indexOfLastElement]));

                        /*
                         * Set the multiplicand as the dividend yield at the
                         * *end* of the month for the year and month given in
                         * the calendar.
                         */
                        getOrCreateEntry(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)).
                                setSAndP500DividendEnd(multiplicand);

                        /*
                         * Add one month to the calendar, and set the
                         * multiplicand as the *start* of the month for the
                         * year and month given in the calendar.
                         */
                        calendar.add(Calendar.MONTH, 1);
                        getOrCreateEntry(calendar.get(Calendar.YEAR),
                                calendar.get(Calendar.MONTH)).
                                setSAndP500DividendStart(multiplicand);
                    }
                }

                // Read the next data line.
                line = reader.readLine();
            }
        }
    }

    /**
     * Sets the S&P 500 values in the database.
     *
     * @throws IOException    Indicates an exception occurred while
     *                        trying to read a file line containing
     *                        S&P 500 valuation data
     * @throws ParseException Indicates either a date, or S&P 500
     *                        valuation level could not be parsed
     */
    private void setSandP500Value() throws IOException, ParseException {

        /*
         * Declare a constant indexing the last expected element in any line.
         * Declare a variable to receive a monthly data object. Declare a
         * variable to receive data items from the lines in the file.
         */
        final int indexOfLastElement = 1;
        MonthlyData data;
        String[] elements;

        /*
         * Declare integer variables. Declare a number to a receive an S&P
         * valuation level.
         */
        int elementsLength, previousMonth, previousYear, thisMonth, thisYear;
        double sAndP500value;

        /*
         * Get a reader for the S&P 500 file. Read the first data line, and
         * cycle while data lines exist.
         */
        reader = getReader(FileType.S_AND_P_500_VALUATION);
        String line = reader.readLine();
        while (null != line) {

            /*
             * Split the elements around one or more tabs. Get the number of
             * distinct data elements. Are there at least the expected number
             * of data elements?
             */
            elements = line.split("\t+");
            elementsLength = elements.length;
            if (indexOfLastElement < elementsLength) {

                /*
                 * There are at least the expected number of data elements.
                 * Parse the first element - which is the date - and set it in
                 * the calendar.
                 */
                calendar.setTime(sAndPValuation.parse(elements[
                        indexOfLastElement - 1]));

                /*
                 * Parse the second element, which is the S&P 500 valuation
                 * level.
                 */
                sAndP500value = usaEnglishFormat.parse(
                        elements[indexOfLastElement]).doubleValue();

                // Get the month and year of this data line.
                thisMonth = calendar.get(Calendar.MONTH);
                thisYear = calendar.get(Calendar.YEAR);

                /*
                 * Decrement the month of the calendar, and get the month and
                 * year of the previous month.
                 */
                calendar.add(Calendar.MONTH, -1);
                previousMonth = calendar.get(Calendar.MONTH);
                previousYear = calendar.get(Calendar.YEAR);

                /*
                 * Get, or create the monthly data for the previous month, and
                 * set its end date with the S&P 500 valuation level.
                 */
                data = getOrCreateEntry(previousYear, previousMonth);
                data.setSAndP500ValueEnd(sAndP500value);

                /*
                 * Get, or create the monthly data for the current month, and
                 * set its start date with the S&P 500 valuation level.
                 */
                data = getOrCreateEntry(thisYear, thisMonth);
                data.setSAndP500ValueStart(sAndP500value);
            }

            // Read the next line.
            line = reader.readLine();
        }
    }

    /**
     * Sets the treasury bill rate in the database.
     *
     * @throws IOException    Indicates an exception occurred while trying to
     *                        read a file line containing treasury bill data
     * @throws ParseException Indicates a date could not be parsed
     */
    private void setTreasuryBill() throws IOException, ParseException {

        /*
         * Get a reader for the treasury bill file, and read the first line. Is
         * there even one line?
         */
        reader = getReader(FileType.TREASURY_BILL);
        String line = reader.readLine();
        if (null != line) {

            /*
             * There is at least one line. Declare a constant indexing the last
             * expected element in any line, and a map of dates to floating
             * point treasury bill interest rate.
             */
            final int indexOfLastElement = 1;
            final Map<Date, Double> map = new HashMap<>();

            /*
             * Declare a variable to receive data items from the lines in the
             * file, and another to receive the number of said elements in any
             * line. Declare and initialize a variable to track the last rate
             * of interest.
             */
            String[] elements;
            int elementsLength;
            double interestRate = 1.;

            // Read the first data line. Cycle while data lines exist.
            line = reader.readLine();
            while (null != line) {

                /*
                 * Split the elements around a comma. Get the number of
                 * distinct elements. Are there at least the expected number of
                 * data elements?
                 */
                elements = line.split("\\s*,\\s*");
                elementsLength = elements.length;
                if (indexOfLastElement < elementsLength) {

                    /*
                     * There are at least the expected number of data elements.
                     * Try to a parse the rate of interest.
                     */
                    try {
                        interestRate = Double.parseDouble(
                                elements[indexOfLastElement]);
                    }

                    /*
                     * Just ignore any exception from a malformed number. We
                     * are only concerned with the last parseable rate of
                     * interest.
                     */ catch (NumberFormatException ignored) {

                        // Algorithmically, there is nothing to do here.
                    }

                    /*
                     * There are at least the expected number of data elements.
                     * Parse the first element, which is the date. Put the last
                     * well-parsed rate into the map indexed by the date.
                     */
                    map.put(governmentAndDividend.parse(
                                    elements[indexOfLastElement - 1]),
                            interestRate);
                }

                // Read the next line.
                line = reader.readLine();
            }

            // Set the treasury bill interest rate using the built-up map.
            setTreasuryBill(map);
        }
    }

    /**
     * Sets the 10-year treasury bill interest rate in the database.
     *
     * @param dateToRateMap A map of dates to rates
     */
    private void setTreasuryBill(@NotNull Map<Date, Double> dateToRateMap) {

        /*
         * Declare a constant representing the first index into a date list,
         * and retrieve the map size. Is the map size greater than zero?
         */
        final int firstIndex = 0;
        final int mapSize = dateToRateMap.size();
        if (firstIndex < mapSize) {

            /*
             * The map size is greater than zero. Get a list of map keys
             * (dates), and sort them by their natural order.
             */
            final List<Date> list = new ArrayList<>(dateToRateMap.keySet());
            list.sort(Comparator.naturalOrder());

            /*
             * Declare variables for a current date, and initialize it to that
             * of the first index in the list. Set this date in the calendar.
             */
            Date dateCurrent = list.get(firstIndex);
            calendar.setTime(dateCurrent);

            /*
             * Declare variables for current year, last year, current month,
             * and last month. Initialize the variables for last year and last
             * month to the last date that is set in the calendar.
             */
            int yearCurrent, yearLast = calendar.get(Calendar.YEAR);
            int monthCurrent, monthLast = calendar.get(Calendar.MONTH);

            /*
             * Declare a variable to track the accumulated rate of interest,
             * and initialize it to the interest rate for the current date.
             * Initialize the number of days in the current month to one.
             */
            double accumulatedInterest = dateToRateMap.get(dateCurrent);
            int numberOfDays = 1;

            /*
             * Declare a variable to receive monthly data, and cycle while
             * there are additional items in the map.
             */
            MonthlyData data;
            for (int i = firstIndex + 1; i < mapSize; ++i) {

                // Get the current date, and set it in the calendar.
                dateCurrent = list.get(i);
                calendar.setTime(dateCurrent);

                /*
                 * Get the current month and year from the calendar. Is the
                 * current month equal to the last month?
                 */
                yearCurrent = calendar.get(Calendar.YEAR);
                monthCurrent = calendar.get(Calendar.MONTH);
                if (monthCurrent == monthLast) {

                    /*
                     * The current month is equal to the last month. Add the
                     * rate of interest for the current date to the accumulated
                     * rate of interest. Increment the number of days.
                     */
                    accumulatedInterest += dateToRateMap.get(dateCurrent);
                    ++numberOfDays;
                }

                // The current month does not equal the last month.
                else {

                    /*
                     * Get or create an entry for the last year and month. Set
                     * the treasury bill interest rate to the average of rates
                     * over the month.
                     */
                    data = getOrCreateEntry(yearLast, monthLast);
                    data.setTreasuryBill(percentToMultiplicand(
                            accumulatedInterest / numberOfDays));

                    // Reset the accumulated interest rate and number of days.
                    accumulatedInterest = dateToRateMap.get(dateCurrent);
                    numberOfDays = 1;

                    /*
                     * Set the last year and last month to the current year and
                     * current month.
                     */
                    yearLast = yearCurrent;
                    monthLast = monthCurrent;
                }
            }

            /*
             * Get or create an entry for the last year and month. Set the
             * treasury bill interest rate to the average of rates over the
             * month.
             */
            data = getOrCreateEntry(yearLast, monthLast);
            data.setTreasuryBill(accumulatedInterest / numberOfDays);
        }
    }

    /**
     * Sets the 10-year treasury bond rate in the database.
     *
     * @throws IOException           Indicates an exception occurred while
     *                               trying to read a file line containing
     *                               10-year treasury bond data
     * @throws NumberFormatException Indicates an exception occurred while
     *                               attempting to parse a floating point value
     * @throws ParseException        Indicates a date could not be parsed
     */
    private void setTreasuryRate() throws IOException, NumberFormatException,
            ParseException {

        /*
         * Get a reader for the treasury bond file, and read the first line. Is
         * there even one line?
         */
        reader = getReader(FileType.TREASURY_BOND);
        String line = reader.readLine();
        if (null != line) {

            /*
             * There is at least one line. Declare a constant indexing the last
             * expected element in any line, and a map of dates to floating
             * point treasury rates.
             */
            final int indexOfLastElement = 1;
            final Map<Date, Double> map = new HashMap<>();

            /*
             * Declare a variable to receive data items from the lines in the
             * file, and another to receive the number of said elements in any
             * line.
             */
            String[] elements;
            int elementsLength;

            // Read the first data line. Cycle while data lines exist.
            line = reader.readLine();
            while (null != line) {

                /*
                 * Split the elements around a comma. Get the number of
                 * distinct elements. Are there at least the expected number of
                 * data elements?
                 */
                elements = line.split("\\s*,\\s*");
                elementsLength = elements.length;
                if (indexOfLastElement < elementsLength) {

                    /*
                     * There are at least the expected number of data elements.
                     * Parse the first element, which is the date, and the
                     * second element which is the treasury rate. Put the rate
                     * into the map indexed by the date.
                     */
                    map.put(governmentAndDividend.parse(
                                    elements[indexOfLastElement - 1]),
                            Double.parseDouble(elements[indexOfLastElement]));
                }

                // Read the next line.
                line = reader.readLine();
            }

            // Set the treasury rates using the built-up map.
            setTreasuryRate(map);
        }
    }

    /**
     * Sets the 10-year treasury bond rate in the database.
     *
     * @param dateToRateMap A map of dates to rates
     */
    private void setTreasuryRate(@NotNull Map<Date, Double> dateToRateMap) {

        /*
         * Declare a constant representing the first index into a date list,
         * and retrieve the map size. Is the map size greater than zero?
         */
        final int firstIndex = 0;
        final int mapSize = dateToRateMap.size();
        if (firstIndex < mapSize) {

            /*
             * The map size is greater than zero. Get a list of map keys
             * (dates), and sort them in their natural order.
             */
            final List<Date> list = new ArrayList<>(dateToRateMap.keySet());
            list.sort(Comparator.naturalOrder());

            /*
             * Declare variables for a current date, and a last date.
             * Initialize the variable for last date to that of the first index
             * in the list. Set this date in the calendar.
             */
            Date dateCurrent, dateLast = list.get(firstIndex);
            calendar.setTime(dateLast);

            /*
             * Declare variables for current year, last year, current month,
             * and last month. Initialize the variables for last year and last
             * month to the last date that is set in the calendar. Declare a
             * variable to receive monthly data, and get (or create) a data
             * item for the last year and last month.
             */
            int yearCurrent, yearLast = calendar.get(Calendar.YEAR);
            int monthCurrent, monthLast = calendar.get(Calendar.MONTH);
            MonthlyData data = getOrCreateEntry(yearLast, monthLast);

            /*
             * Declare and initialize a variable for the rate for the last
             * date. Set the starting treasury rate for the last year and last
             * month. Cycle for each remaining dates in the list.
             */
            double rateLast = percentToMultiplicand(dateToRateMap.get(dateLast));
            data.setTreasuryRateStart(rateLast);
            for (int i = firstIndex + 1; i < mapSize; ++i) {

                // Get the current date, and set it in the calendar.
                dateCurrent = list.get(i);
                calendar.setTime(dateCurrent);

                /*
                 * Get the current month and year from the calendar. Is the
                 * current month not equal to the last month?
                 */
                yearCurrent = calendar.get(Calendar.YEAR);
                monthCurrent = calendar.get(Calendar.MONTH);
                if (monthCurrent != monthLast) {

                    /*
                     * The current month does not equal the last month. Get the
                     * data item for the last year and last month. The entry
                     * better already exist! Get the rate for the last year and
                     * month, and use it to set the ending treasury rate.
                     */
                    data = getEntry(yearLast, monthLast);
                    rateLast = percentToMultiplicand(dateToRateMap.get(dateLast));
                    data.setTreasuryRateEnd(rateLast);

                    /*
                     * Get (or create) a data item for the current year and
                     * current month. Set its starting treasury rate.
                     */
                    data = getOrCreateEntry(yearCurrent, monthCurrent);
                    data.setTreasuryRateStart(rateLast);
                }

                // Update the last date, last year, and last month.
                dateLast = dateCurrent;
                yearLast = yearCurrent;
                monthLast = monthCurrent;
            }

            /*
             * Almost done. Get the monthly data item for the last year and
             * last month. The entry better already exist! Set its ending
             * treasury rate.
             */
            data = getEntry(yearLast, monthLast);
            data.setTreasuryRateEnd(dateToRateMap.get(dateLast));
        }
    }

    private enum FileType {

        // The alternate inflation file type
        ALTERNATE_INFLATION,

        // The fed funds file type
        FED_FUNDS,

        // The historical inflation file type
        HISTORICAL_INFLATION,

        // The S&P 500 dividend file type
        S_AND_P_500_DIVIDEND,

        // The S&P 500 valuation file type
        S_AND_P_500_VALUATION,

        // The treasury bill file type
        TREASURY_BILL,

        // The 10-year treasury bond interest file type
        TREASURY_BOND
    }
}
