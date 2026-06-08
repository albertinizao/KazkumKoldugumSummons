package com.pathfinder.summons.domain.model;

import lombok.Value;

@Value
public class DailyUses {
    public static final int DEFAULT_MAXIMUM = 6;
    public static final int DEFAULT_REMAINING = 4;

    int maximum;
    int remaining;

    public DailyUses(int maximum, int remaining) {
        if (maximum < 0) {
            throw new IllegalArgumentException("Maximum uses cannot be negative");
        }
        if (remaining < 0 || remaining > maximum) {
            throw new IllegalArgumentException("Remaining uses must be between 0 and maximum");
        }
        this.maximum = maximum;
        this.remaining = remaining;
    }

    public DailyUses consumeOne() {
        if (remaining <= 0) {
            return this;
        }
        return new DailyUses(maximum, remaining - 1);
    }

    public DailyUses increase(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to increase must be positive");
        }
        int newRemaining = Math.min(maximum, remaining + amount);
        return new DailyUses(maximum, newRemaining);
    }

    public DailyUses decrease(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount to decrease must be positive");
        }
        int newRemaining = Math.max(0, remaining - amount);
        return new DailyUses(maximum, newRemaining);
    }

    public DailyUses reset() {
        return new DailyUses(maximum, maximum);
    }

    public DailyUses updateMaximum(int newMaximum) {
        if (newMaximum < 0) {
            throw new IllegalArgumentException("Maximum uses cannot be negative");
        }
        int newRemaining = Math.min(remaining, newMaximum);
        return new DailyUses(newMaximum, newRemaining);
    }

    public static DailyUses defaultDailyUses() {
        return new DailyUses(DEFAULT_MAXIMUM, DEFAULT_REMAINING);
    }

    public static DailyUses normalize(int maximum, int remaining) {
        int normalizedMaximum = Math.max(0, maximum);
        int normalizedRemaining = Math.max(0, Math.min(remaining, normalizedMaximum));
        return new DailyUses(normalizedMaximum, normalizedRemaining);
    }
}
