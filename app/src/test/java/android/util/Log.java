package android.util;

import androidx.annotation.Nullable;

/**
 * Mock of android.util.Log for testing.
 */
public class Log {

    /**
     * Prints the tag and message to stdout.
     *
     * @param tag The tag to print.
     * @param msg The message to print.
     * @return 0
     */
    public static int d(@Nullable String tag, @Nullable String msg) {
        System.out.println("DEBUG: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Prints the tag and message to stdout.
     *
     * @param tag The tag to print.
     * @param msg The message to print.
     * @return 0
     */
    public static int i(@Nullable String tag, @Nullable String msg) {
        System.out.println("INFO: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Prints the tag and message to stdout.
     *
     * @param tag The tag to print.
     * @param msg The message to print.
     * @return 0
     */
    public static int w(@Nullable String tag, @Nullable String msg) {
        System.out.println("WARN: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Prints the tag and message to stdout.
     *
     * @param tag The tag to print.
     * @param msg The message to print.
     * @return 0
     */
    public static int e(@Nullable String tag, @Nullable String msg) {
        System.out.println("ERROR: " + tag + ": " + msg);
        return 0;
    }

    /**
     * Prints the tag and message to stdout.
     *
     * @param tag The tag to print.
     * @param msg The message to print.
     * @param e   The exception to print.
     * @return 0
     */
    public static int e(@Nullable String tag, @Nullable String msg, @Nullable Throwable e) {
        System.out.println("ERROR: " + tag + ": " + msg);
        if (e != null) {
            e.printStackTrace();
        }
        return 0;
    }

}
