package com.rosewood.payroll;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;

/**
 * Compares money amounts by value, so 150 and 150.0000 are equal. Tax bands and gross pay
 * policies return unrounded amounts; only PayrollService rounds them to two decimal places.
 */
public final class MoneyAssertions {

    private MoneyAssertions() {
    }

    public static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "expected " + expected + " but was " + actual);
    }
}
