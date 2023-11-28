package edu.uga.cs.shopsync.utils;

public class DataWrapper<T> {

    private T object;

    public DataWrapper() {
        object = null;
    }

    public DataWrapper(T object) {
        this.object = object;
    }

    public T get() {
        return object;
    }

    public void set(T object) {
        this.object = object;
    }
}
