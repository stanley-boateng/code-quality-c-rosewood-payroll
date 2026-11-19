package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;

/** Hours worked in a pay period and the hourly rate they are paid at. Always valid once created. */
public record WorkPeriod(BigDecimal hoursWorked, BigDecimal hourlyRate) {

    public WorkPeriod {
        Objects.requireNonNull(hoursWorked, "hoursWorked");
        Objects.requireNonNull(hourlyRate, "hourlyRate");
        if (hoursWorked.signum() < 0) {
            throw new InvalidWorkPeriodException("Hours worked must not be negative: " + hoursWorked);
        }
        if (hourlyRate.signum() <= 0) {
            throw new InvalidWorkPeriodException("Hourly rate must be greater than zero: " + hourlyRate);
        }
    }

    /**
     * Builds a period from text such as "37.5" and "20.00", so values are exact decimals.
     *
     * @throws InvalidWorkPeriodException if either value is not a number, or is out of range
     */
    public static WorkPeriod of(String hoursWorked, String hourlyRate) {
        return new WorkPeriod(parse("Hours worked", hoursWorked), parse("Hourly rate", hourlyRate));
    }

    private static BigDecimal parse(String field, String value) {
        Objects.requireNonNull(value, field);
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new InvalidWorkPeriodException(field + " is not a number: '" + value + "'", e);
        }
    }
}