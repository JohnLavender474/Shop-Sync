package edu.uga.cs.shopsync.utils;

/**
 * Utility methods.
 */
public class UtilMethods {

    /**
     * Rounds the given value to the given number of decimal places.
     *
     * @param value         the value to round
     * @param decimalPlaces the number of decimal places to round to
     * @return the rounded value
     */
    public static String truncateToDecimalPlaces(double value, long decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", value);
    }

    /**
     * Checks that the given string is a long.
     *
     * @param s the string
     * @return if the string is a long
     */
    public static boolean isLong(String s) {
        boolean isLong = false;
        try {
            Long.parseLong(s);
            isLong = true;
        } catch (NumberFormatException ignored) {
        }
        return isLong;
    }

    /**
     * Checks the the given string is a double.
     *
     * @param s the string
     * @return if the string is a double
     */
    public static boolean isDouble(String s) {
        boolean isDouble = false;
        try {
            Double.parseDouble(s);
            isDouble = true;
        } catch (NumberFormatException ignored) {
        }
        return isDouble;
    }

}
