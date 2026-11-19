package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Turns the text values given to tax band factories into numbers, naming the band and field on failure.
 */
final class BandValues {

    private BandValues() {
    }

    /**
     * @throws IllegalArgumentException if the value is not a number
     */
    static BigDecimal parse(String bandName, String field, String value) {
        Objects.requireNonNull(value, bandName + " " + field);
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    bandName + ": " + field + " is not a number: '" + value + "'", e);
        }
    }
}
