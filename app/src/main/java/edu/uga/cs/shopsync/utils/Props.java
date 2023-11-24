package edu.uga.cs.shopsync.utils;

import android.util.Pair;

import java.util.HashMap;

/**
 * A HashMap with type-safe getters.
 */
public class Props extends HashMap<String, Object> {

    @SafeVarargs
    public static Props of(Pair<String, Object>... pairs) {
        Props props = new Props();
        for (Pair<String, Object> pair : pairs) {
            props.put(pair.first, pair.second);
        }
        return props;
    }

    /**
     * Get the value associated with the given key.
     *
     * @param key  The key to look up.
     * @param type The type of the value to return.
     * @param <T>  The type of the value to return.
     * @return The value associated with the given key.
     */
    public <T> T get(String key, Class<T> type) {
        return type.cast(get(key));
    }

    /**
     * Get the value associated with the given key, or the default value if the key is not present.
     *
     * @param key          The key to look up.
     * @param type         The type of the value to return.
     * @param defaultValue The default value to return if the key is not present.
     * @param <T>          The type of the value to return.
     * @return The value associated with the given key, or the default value if the key is not
     * present.
     */
    public <T> T getOrDefault(String key, Class<T> type, T defaultValue) {
        T value = get(key, type);
        return value != null ? value : defaultValue;
    }
}
