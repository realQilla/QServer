package net.qilla.util;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.Format;

public final class NumberUtil {

    private NumberUtil() {
    }

    /**
     * Locks a set value within two set integer values
     *
     * @param min   The minimum the integer can be
     * @param max   The maximum the integer can be
     * @param value The value to validate
     *
     * @return Returns an integer within the specified values
     */

    public static int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Locks a set value within two set integer values
     *
     * @param min   The minimum the float can be
     * @param max   The maximum the float can be
     * @param value The value to validate
     *
     * @return Returns an integer within the specified values
     */

    public static float clamp(float min, float max, float value) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * Converts a double value to utilize commas
     *
     * @param number The double to convert
     *
     * @return Returns a formatted double to utilize commas
     */

    public static @NotNull String numberComma(double number) {
        return String.format("%,d", (int) number);
    }

    /**
     * Converts a long value to utilize commas
     *
     * @param number The long to convert
     *
     * @return Returns a formatted long to utilize commas
     */

    public static @NotNull String numberComma(long number) {
        return String.format("%,d", number);
    }

    /**
     * Takes two values and returns a percentage
     *
     * @param current The current value
     * @param total   The total value
     *
     * @return Returns a percentage string
     */

    public static @NotNull String numberPercentage(double total, double current) {
        if(total == 0) return "0%";
        double percentage = (current / total) * 100;
        return (int) percentage + "%";
    }

    private static final char[] SUFFIXES = {'k', 'm', 'b', 't', 'q'};

    /**
     * Utility method to format a number to use letters for easier reading
     *
     * @param number     The number to format
     * @param capitalize If the letters should be capitalized
     *
     * @return Returns a new formatted string
     */

    public static @NotNull String numberChar(double number, boolean capitalize) {
        if(number < 1) return String.valueOf((int) number);
        if(number < 1000) return String.valueOf((int) number);

        int exp = (int) (Math.log10(number) / 3);
        if(exp > SUFFIXES.length) return "NaN";

        char suffix = SUFFIXES[exp - 1];
        if(capitalize) suffix = Character.toUpperCase(suffix);

        double scaledNumber = number / Math.pow(1000, exp);
        return String.format("%.1f%c", scaledNumber, suffix).replace(".0", ""); // Remove unnecessary decimals
    }

    /**
     * Cuts off a specified amount of decimals off of a double
     *
     * @param number   The double to modify
     * @param decimals The amount of decimals to keep
     *
     * @return Returns a new string
     */

    public static @NotNull String decimalTruncation(double number, int decimals) {
        Format format = new DecimalFormat("#." + "#".repeat(decimals));
        return format.format(number);
    }

    /**
     * Converts an integer into a roman numeral
     *
     * @param number The integer to convert
     *
     * @return Returns a roman numeral string
     */

    public static String romanNumeral(int number) {
        if(number <= 0 || number > 9999) return String.valueOf(number);

        String[] romanNumerals = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};

        StringBuilder roman = new StringBuilder();
        for(int i = 0; i < values.length; i++) {
            while(number >= values[i]) {
                roman.append(romanNumerals[i]);
                number -= values[i];
            }
        }
        return roman.toString();
    }

    private static boolean isWithin(double angle, double target, double range) {
        double min = normalizeAngle(target - range);
        double max = normalizeAngle(target + range);

        return (min < max) ? (angle >= min && angle <= max) : (angle >= min || angle <= max);
    }

    private static double normalizeAngle(double angle) {
        angle = angle % 360;
        return (angle > 180) ? angle - 360 : (angle < -180) ? angle + 360 : angle;
    }
}