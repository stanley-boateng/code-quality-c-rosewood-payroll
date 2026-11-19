package com.rosewood.payroll.grosspay;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkPeriodTest {

    @Test
    void parsesHoursAndRateAsExactDecimalsIgnoringSurroundingSpaces() {
        WorkPeriod period = WorkPeriod.of(" 37.5 ", "20.00");

        assertAll(
                () -> assertEquals(new BigDecimal("37.5"), period.hoursWorked()),
                () -> assertEquals(new BigDecimal("20.00"), period.hourlyRate()));
    }

    @Test
    void acceptsZeroHours() {
        assertEquals(BigDecimal.ZERO, WorkPeriod.of("0", "20").hoursWorked());
    }

    @Test
    void rejectsNegativeHours() {
        InvalidWorkPeriodException exception = assertThrows(InvalidWorkPeriodException.class,
                () -> WorkPeriod.of("-5", "20"));
        assertEquals("Hours worked must not be negative: -5", exception.getMessage());
    }

    @ParameterizedTest(name = "rate {0} is rejected")
    @ValueSource(strings = {"0", "-1"})
    void rejectsRateThatIsNotPositive(String rate) {
        InvalidWorkPeriodException exception = assertThrows(InvalidWorkPeriodException.class,
                () -> WorkPeriod.of("40", rate));
        assertEquals("Hourly rate must be greater than zero: " + rate, exception.getMessage());
    }

    @Test
    void rejectsHoursThatAreNotANumber() {
        InvalidWorkPeriodException exception = assertThrows(InvalidWorkPeriodException.class,
                () -> WorkPeriod.of("forty", "20"));
        assertAll(
                () -> assertEquals("Hours worked is not a number: 'forty'",
                        exception.getMessage()),
                () -> assertInstanceOf(NumberFormatException.class, exception.getCause()));
    }

    @Test
    void rejectsRateThatIsNotANumber() {
        InvalidWorkPeriodException exception = assertThrows(InvalidWorkPeriodException.class,
                () -> WorkPeriod.of("40", "twenty"));
        assertEquals("Hourly rate is not a number: 'twenty'", exception.getMessage());
    }

    @Test
    void rejectsMissingText() {
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> WorkPeriod.of(null, "20"));
        assertEquals("Hours worked", exception.getMessage());
    }
}
