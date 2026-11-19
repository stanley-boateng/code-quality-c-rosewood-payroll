package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A band covering a range of gross pay, taxing the whole gross at one rate.
 */
public final class RateTaxBand implements TaxBand {

    private final String name;
    private final BigDecimal from;  // inclusive
    private final BigDecimal upTo;  // exclusive; null only for the open-ended top band
    private final BigDecimal rate;

    private RateTaxBand(String name, BigDecimal from, BigDecimal upTo, BigDecimal rate) {
        this.name = Objects.requireNonNull(name, "name");
        this.from = Objects.requireNonNull(from, "from");
        this.rate = Objects.requireNonNull(rate, "rate");
        this.upTo = upTo;
        if (from.signum() < 0) {
            throw new IllegalArgumentException(name + ": lower limit must not be negative: " + from);
        }
        if (upTo != null && upTo.compareTo(from) <= 0) {
            throw new IllegalArgumentException(name + ": upper limit must be above lower limit");
        }
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(name + ": rate must be between 0 and 1: " + rate);
        }
    }

    /**
     * A band from {@code from} (inclusive) up to {@code upTo} (exclusive).
     *
     * @throws IllegalArgumentException if a value is not a number or the band is invalid
     */
    public static RateTaxBand between(String name, String from, String upTo, String rate) {
        return new RateTaxBand(name,
                parse(name, "lower limit", from),
                parse(name, "upper limit", upTo),
                parse(name, "rate", rate));
    }

    /**
     * An open-ended band from {@code from} (inclusive) upwards.
     *
     * @throws IllegalArgumentException if a value is not a number or the band is invalid
     */
    public static RateTaxBand from(String name, String from, String rate) {
        return new RateTaxBand(name,
                parse(name, "lower limit", from),
                null,
                parse(name, "rate", rate));
    }

    private static BigDecimal parse(String bandName, String field, String value) {
        Objects.requireNonNull(value, bandName + " " + field);
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    bandName + ": " + field + " is not a number: '" + value + "'", e);
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean appliesTo(BigDecimal grossPay) {
        boolean atOrAboveFrom = grossPay.compareTo(from) >= 0;
        boolean belowUpTo = upTo == null || grossPay.compareTo(upTo) < 0;
        return atOrAboveFrom && belowUpTo;
    }

    @Override
    public BigDecimal taxFor(BigDecimal grossPay) {
        return grossPay.multiply(rate);
    }
}
