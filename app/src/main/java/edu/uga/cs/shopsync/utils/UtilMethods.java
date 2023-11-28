package edu.uga.cs.shopsync.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Utility methods.
 */
public class UtilMethods {

    /**
     * Creates a mutable list from the given objects
     *
     * @param objects the objects to add to the list
     * @param <T>     the type of the objects
     * @return the mutable list
     */
    @SafeVarargs
    public static <T> List<T> mutableListOf(T... objects) {
        return new ArrayList<>(Arrays.asList(objects));
    }

    /**
     * Rounds the given value to the given number of decimal places.
     *
     * @param value         the value to round
     * @param decimalPlaces the number of decimal places to round to
     * @return the rounded value
     */
    public static String roundToDecimalPlaces(double value, long decimalPlaces) {
        return String.format("%." + decimalPlaces + "f", value);
    }

}
