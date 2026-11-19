package com.rosewood.payroll.tax;

import static com.rosewood.payroll.MoneyAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class RateTaxBandTest {

    private static final BigDecimal GROSS = new BigDecimal("2000");

    @ParameterizedTest(name = "gross {0} in MEDIUM: {1}")
    @CsvSource({"999.99, false", "1000, true", "4999.99, true", "5000, false"})
    void betweenCoversLowerLimitUpToButNotIncludingUpperLimit(String gross, boolean covered) {
        RateTaxBand medium = RateTaxBand.between("MEDIUM", "1000", "5000", "0.15");

        assertEquals(covered, medium.appliesTo(new BigDecimal(gross)));
    }

    @ParameterizedTest(name = "gross {0} in HIGH: {1}")
    @CsvSource({"4999.99, false", "5000, true", "1000000, true"})
    void fromCoversLowerLimitAndEverythingAbove(String gross, boolean covered) {
        RateTaxBand high = RateTaxBand.from("HIGH", "5000", "0.25");

        assertEquals(covered, high.appliesTo(new BigDecimal(gross)));
    }

    @Test
    void taxesTheWholeGrossAtTheRate() {
        RateTaxBand medium = RateTaxBand.between("MEDIUM", "1000", "5000", "0.15");

        assertAmount("300", medium.calculateTax(GROSS));
    }

    @Test
    void reportsItsName() {
        assertEquals("HIGH", RateTaxBand.from("HIGH", "5000", "0.25").name());
    }

    @Test
    void acceptsZeroLowerLimit() {
        RateTaxBand low = RateTaxBand.between("LOW", "0", "1000", "0.05");

        assertTrue(low.appliesTo(BigDecimal.ZERO));
    }

    @Test
    void rejectsNegativeLowerLimit() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RateTaxBand.between("LOW", "-1", "1000", "0.05"));
        assertEquals("LOW: lower limit must not be negative: -1", exception.getMessage());
    }

    @ParameterizedTest(name = "upper limit {0} with lower limit 1000 is rejected")
    @ValueSource(strings = {"1000", "999"})
    void rejectsUpperLimitNotAboveLowerLimit(String upperLimit) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RateTaxBand.between("MEDIUM", "1000", upperLimit, "0.15"));
        assertEquals("MEDIUM: upper limit must be above lower limit", exception.getMessage());
    }

    @ParameterizedTest(name = "rate {0} gives tax {1} on 2000")
    @CsvSource({"0, 0", "1, 2000"})
    void acceptsRatesFromZeroToOne(String rate, String expectedTax) {
        RateTaxBand band = RateTaxBand.from("ANY", "0", rate);

        assertAmount(expectedTax, band.calculateTax(GROSS));
    }

    @ParameterizedTest(name = "rate {0} is rejected")
    @ValueSource(strings = {"-0.01", "1.01"})
    void rejectsRateOutsideZeroToOne(String rate) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RateTaxBand.from("ANY", "0", rate));
        assertEquals("ANY: rate must be between 0 and 1: " + rate, exception.getMessage());
    }

    @Test
    void rejectsValueThatIsNotANumber() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> RateTaxBand.between("LOW", "zero", "1000", "0.05"));
        assertAll(
                () -> assertEquals("LOW: lower limit is not a number: 'zero'",
                        exception.getMessage()),
                () -> assertInstanceOf(NumberFormatException.class, exception.getCause()));
    }
}
