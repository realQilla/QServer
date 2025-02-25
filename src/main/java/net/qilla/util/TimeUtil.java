package net.qilla.util;

import org.jetbrains.annotations.NotNull;

import java.text.DateFormat;

public final class TimeUtil {

    public static @NotNull String date(long millis) {
        return DateFormat.getDateTimeInstance().format(millis);
    }

    /**
     * Utility method to convert milliseconds to a readable format
     * @param ms Milliseconds to count
     * @param shortForm If the final string should use letters or the full word
     * @return Returns a new string containing the formatted time
     */

    public static @NotNull String remaining(long ms, boolean shortForm) {
        if (ms < 1000) return shortForm ? "<1s" : "Less than one second";

        final long MS_IN_SECOND = 1000;
        final long SECONDS_IN_MINUTE = 60;
        final long MINUTES_IN_HOUR = 60;
        final long HOURS_IN_DAY = 24;
        final double DAYS_IN_MONTH = 30.44;
        final long DAYS_IN_YEAR = 365;

        long seconds = ms / MS_IN_SECOND;
        long minutes = seconds / SECONDS_IN_MINUTE;
        long hours = minutes / MINUTES_IN_HOUR;
        long days = hours / HOURS_IN_DAY;

        long years = days / DAYS_IN_YEAR;
        days %= DAYS_IN_YEAR;

        long months = (long) (days / DAYS_IN_MONTH);
        days %= (long) DAYS_IN_MONTH;

        seconds %= SECONDS_IN_MINUTE;
        minutes %= MINUTES_IN_HOUR;
        hours %= HOURS_IN_DAY;

        if (shortForm) {
            if (years > 0) return years + "y";
            if (months > 0) return months + "M";
            if (days > 0) return days + "d";
            if (hours > 0) return hours + "h";
            if (minutes > 0) return minutes + "m";
            return seconds + "s";
        }

        StringBuilder result = new StringBuilder();
        appendTime(result, years, "year");
        appendTime(result, months, "month");
        appendTime(result, days, "day");
        appendTime(result, hours, "hour");
        appendTime(result, minutes, "minute");
        appendTime(result, seconds, "second");

        return result.toString().trim();
    }

    private static void appendTime(StringBuilder result, long value, String unit) {
        if (value > 0) {
            result.append(value).append(" ").append(unit);
            if (value > 1) result.append("s");
            result.append(" ");
        }
    }
}
