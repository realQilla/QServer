package net.qilla.command;

import java.time.temporal.ChronoUnit;

public enum TimeUnit {
    YEAR(ChronoUnit.YEARS.getDuration().toMillis()),
    MONTH(ChronoUnit.MONTHS.getDuration().toMillis()),
    DAY(ChronoUnit.DAYS.getDuration().toMillis()),
    HOUR(ChronoUnit.HOURS.getDuration().toMillis()),
    MINUTE(ChronoUnit.MINUTES.getDuration().toMillis()),
    SECOND(ChronoUnit.SECONDS.getDuration().toMillis());

    private final long duration;

    TimeUnit(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }
}