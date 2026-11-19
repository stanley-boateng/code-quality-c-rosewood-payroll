package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * A band covering a range of gross pay, taxing the whole gross at one rate.
 */
public final class RateTaxBand implements TaxBand {

    private final String name;
    private final BigDecimal lowerLimitInclusive;
    private final BigDecimal upperLimitExclusive;
    private final BigDecimal rate;

    /**
     * @param upperLimitExclusive null only for the open-ended top band
     */
    private RateTaxBand(String name, BigDecimal lowerLimitInclusive,
            BigDecimal upperLimitExclusive, BigDecimal rate) {
        this.name = Objects.requireNonNull(name, "name");
        this.lowerLimitInclusive = Objects.requireNonNull(lowerLimitInclusive, "lowerLimit");
        this.upperLimitExclusive = upperLimitExclusive;
        this.rate = Objects.requireNonNull(rate, "rate");
        if (lowerLimitInclusive.signum() < 0) {
            throw new IllegalArgumentException(
                    name + ": lower limit must not be negative: " + lowerLimitInclusive);
        }
        boolean upperLimitTooLow = upperLimitExclusive != null
                && upperLimitExclusive.compareTo(lowerLimitInclusive) <= 0;
        if (upperLimitTooLow) {
            throw new IllegalArgumentException(name + ": upper limit must be above lower limit");
        }
        if (rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException(name + ": rate must be between 0 and 1: " + rate);
        }
    }

    /**
     * A band from {@code lowerLimit} (inclusive) up to {@code upperLimit} (exclusive).
     *
     * @throws IllegalArgumentException if a value is not a number or the band is invalid
     */
    public static RateTaxBand between(
            String name, String lowerLimit, String upperLimit, String rate) {
        return new RateTaxBand(name,
                parse(name, "lower limit", lowerLimit),
                parse(name, "upper limit", upperLimit),
                parse(name, "rate", rate));
    }

    /**
     * An open-ended band from {@code lowerLimit} (inclusive) upwards.
     *
     * @throws IllegalArgumentException if a value is not a number or the band is invalid
     */
    public static RateTaxBand from(String name, String lowerLimit, String rate) {
        return new RateTaxBand(name,
                parse(name, "lower limit", lowerLimit),
                null,
                parse(name, "rate", rate));
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public boolean appliesTo(BigDecimal grossPay) {
        boolean atOrAboveLowerLimit = grossPay.compareTo(lowerLimitInclusive) >= 0;
        boolean belowUpperLimit =
                upperLimitExclusive == null || grossPay.compareTo(upperLimitExclusive) < 0;
        return atOrAboveLowerLimit && belowUpperLimit;
    }

    @Override
    public BigDecimal calculateTax(BigDecimal grossPay) {
        return grossPay.multiply(rate);
    }

    private static BigDecimal parse(String bandName, String field, String value) {
        Objects.requireNonNull(value, bandName + " " + field);
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    bandName + ": " + field + " is not a number: '" + value + "'", exception);
        }
    }
}
