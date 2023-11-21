package edu.uga.cs.shopsync.backend.exceptions;

public class IllegalNullValueException extends RuntimeException {
    public IllegalNullValueException(String message) {
        super(message);
    }
}
