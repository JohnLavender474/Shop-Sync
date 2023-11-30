package edu.uga.cs.shopsync.utils;

import androidx.annotation.Nullable;

/**
 * This class is used to wrap data that is passed to a callback function. This is useful when the
 * callback function needs to modify the data that is passed to it.
 *
 * @param <T> The type of data to wrap.
 */
public class DataWrapper<T> {

    private T data;

    /**
     * Default constructor for DataWrapper. This constructor initializes the data to null.
     */
    public DataWrapper() {
        data = null;
    }

    /**
     * Constructor for DataWrapper. This constructor initializes the data to the specified value.
     *
     * @param data The data to wrap.
     */
    public DataWrapper(@Nullable T data) {
        this.data = data;
    }

    /**
     * Returns the data that is wrapped by this object.
     *
     * @return The data that is wrapped by this object.
     */
    public @Nullable T get() {
        return data;
    }

    /**
     * Sets the data that is wrapped by this object.
     *
     * @param data The data to wrap.
     */
    public void set(@Nullable T data) {
        this.data = data;
    }
}
