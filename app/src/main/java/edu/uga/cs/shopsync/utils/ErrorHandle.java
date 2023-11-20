package edu.uga.cs.shopsync.utils;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public record ErrorHandle(@NonNull ErrorType errorType, @NonNull Map<String, Object> props,
                          @NonNull String errorMessage) {

    public ErrorHandle(@NonNull ErrorType errorType, @NonNull String errorMessage) {
        this(errorType, new HashMap<>(), errorMessage);
    }

    @NonNull
    @Override
    public String toString() {
        return "ErrorHandle{" +
                "errorType=" + errorType +
                ", props=" + props +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }

}