package com.rosewood.payroll.grosspay;

import static com.rosewood.payroll.MoneyAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class OvertimeGrossPayPolicyTest {

    private static final BigDecimal STANDARD_HOURS = new BigDecimal("40");
    private static final BigDecimal TIME_AND_A_HALF_PREMIUM = new BigDecimal("0.5");

    private final GrossPayPolicy policy =
            new OvertimeGrossPayPolicy(STANDARD_HOURS, TIME_AND_A_HALF_PREMIUM);

    @ParameterizedTest(name = "{0} h at {1} -> {2}")
    @CsvSource({"0, 20, 0", "39.5, 20, 790", "40, 20, 800"})
    void paysHourlyRateUpToStandardHours(String hours, String rate, String expectedPay) {
        assertAmount(expectedPay, policy.calculateGrossPay(WorkPeriod.of(hours, rate)));
    }

    @ParameterizedTest(name = "{0} h at {1} -> {2}")
    @CsvSource({"40.5, 20, 815", "50, 30, 1650"})
    void paysPremiumOnHoursAboveStandardHours(String hours, String rate, String expectedPay) {
        assertAmount(expectedPay, policy.calculateGrossPay(WorkPeriod.of(hours, rate)));
    }

    @Test
    void paysNoExtraForOvertimeWhenPremiumIsZero() {
        GrossPayPolicy noPremium = new OvertimeGrossPayPolicy(STANDARD_HOURS, BigDecimal.ZERO);

        assertAmount("1000", noPremium.calculateGrossPay(WorkPeriod.of("50", "20")));
    }

    @ParameterizedTest(name = "standard hours {0} is rejected")
    @ValueSource(strings = {"0", "-1"})
    void rejectsStandardHoursThatAreNotPositive(String standardHours) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new OvertimeGrossPayPolicy(
                        new BigDecimal(standardHours), TIME_AND_A_HALF_PREMIUM));
        assertEquals("Standard hours must be greater than zero: " + standardHours,
                exception.getMessage());
    }

    @Test
    void rejectsNegativeOvertimePremium() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new OvertimeGrossPayPolicy(STANDARD_HOURS, new BigDecimal("-0.01")));
        assertEquals("Overtime premium must not be negative: -0.01", exception.getMessage());
    }
}
