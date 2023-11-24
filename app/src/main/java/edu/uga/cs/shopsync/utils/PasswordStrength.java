package edu.uga.cs.shopsync.utils;

import android.graphics.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.shopsync.R;

/**
 * Credit to Sylvain Saurel for this code snippet:
 * <a href="https://www.ssaurel.com/blog/develop-a-password-strength-calculator-application-for-android/">
 * Develop a Password Strength Calculator Application for Android</a>
 */
public enum PasswordStrength {

    WEAK(R.string.weak, Color.parseColor("#61ad85")),
    MEDIUM(R.string.medium, Color.parseColor("#4d8a6a")),
    STRONG(R.string.strong, Color.parseColor("#3a674f"));

    /**
     * A record containing the result of a password strength calculation.
     *
     * @param strength    The strength of the password.
     * @param criteriaMet A map of criteria met by the password.
     */
    public record PasswordStrengthCalculationResult(
            PasswordStrength strength, Map<PasswordStrengthCriteria, Boolean> criteriaMet) {
    }

    /**
     * An enum representing the criteria that a password must meet to be considered strong.
     */
    public enum PasswordStrengthCriteria {
        MEET_MIN_LENGTH,
        HAS_SPECIAL_CHAR,
        HAS_UPPER_CASE,
        HAS_LOWER_CASE,
        HAS_ALPHANUMERIC,
        HAS_DIGIT
    }

    public static final int MIN_LENGTH = 8;

    public final int message;
    public final int color;

    PasswordStrength(int message, int color) {
        this.message = message;
        this.color = color;
    }

    /**
     * Create a map of criteria to booleans, with all booleans set to false.
     *
     * @return A map of criteria to booleans, with all booleans set to false.
     */
    public static Map<PasswordStrengthCriteria, Boolean> createCriteriaMap() {
        Map<PasswordStrengthCriteria, Boolean> criteria = new HashMap<>();
        Arrays.stream(PasswordStrengthCriteria.values()).forEach(c -> criteria.put(c, false));
        return criteria;
    }

    /**
     * Calculate the strength of a password.
     *
     * @param password The password to calculate the strength of.
     * @return A record containing the strength of the password and a map of criteria met by the
     * password.
     */
    public static PasswordStrengthCalculationResult calculate(String password) {
        Map<PasswordStrengthCriteria, Boolean> criteria = createCriteriaMap();

        if (password == null) {
            return new PasswordStrengthCalculationResult(WEAK, criteria);
        }

        int score = 0;
        // boolean indicating if password has an upper case
        boolean upper = false;
        // boolean indicating if password has a lower case
        boolean lower = false;
        // boolean indicating if password has at least one digit
        boolean digit = false;
        // boolean indicating if password has a least one special char
        boolean specialChar = false;

        for (int i = 0; i < password.length(); i++) {
            char c = password.charAt(i);

            if (!specialChar && !Character.isLetterOrDigit(c)) {
                criteria.replace(PasswordStrengthCriteria.HAS_SPECIAL_CHAR, true);
                score++;
                specialChar = true;
            } else {
                if (!digit && Character.isDigit(c)) {
                    criteria.replace(PasswordStrengthCriteria.HAS_DIGIT, true);
                    score++;
                    digit = true;
                } else {
                    criteria.replace(PasswordStrengthCriteria.HAS_ALPHANUMERIC, true);
                    if (!upper || !lower) {
                        if (Character.isUpperCase(c)) {
                            criteria.replace(PasswordStrengthCriteria.HAS_UPPER_CASE, true);
                            upper = true;
                        } else {
                            criteria.replace(PasswordStrengthCriteria.HAS_LOWER_CASE, true);
                            lower = true;
                        }

                        if (upper && lower) {
                            score++;
                        }
                    }
                }
            }
        }

        int length = password.length();

        if (length > MIN_LENGTH) {
            criteria.replace(PasswordStrengthCriteria.MEET_MIN_LENGTH, true);
            score++;
        } else if (length < MIN_LENGTH) {
            score = 0;
        }

        PasswordStrength passwordStrength = switch (score) {
            case 0 -> WEAK;
            case 1 -> MEDIUM;
            default -> STRONG;
        };

        return new PasswordStrengthCalculationResult(passwordStrength, criteria);
    }
}
