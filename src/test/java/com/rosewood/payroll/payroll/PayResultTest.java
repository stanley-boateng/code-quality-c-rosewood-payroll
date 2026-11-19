package com.rosewood.payroll.payroll;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rosewood.payroll.employee.Employee;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class PayResultTest {

    private final Employee efua = new Employee("e1", "Efua Sarpong");

    @Test
    void acceptsAmountsThatAddUpWhateverTheirScale() {
        assertDoesNotThrow(() -> new PayResult(efua,
                new BigDecimal("1000.00"), new BigDecimal("150"), new BigDecimal("850.0")));
    }

    @Test
    void rejectsAmountsThatDoNotAddUp() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new PayResult(efua,
                        new BigDecimal("1000.00"), new BigDecimal("150.00"),
                        new BigDecimal("900.00")));
        assertEquals("Payslip does not add up: gross 1000.00 - tax 150.00 != net 900.00",
                exception.getMessage());
    }

    @Test
    void showsOnlyTheEmployeeIdWhenPrinted() {
        PayResult result = new PayResult(efua,
                new BigDecimal("1000.00"), new BigDecimal("150.00"), new BigDecimal("850.00"));

        assertEquals("PayResult[employeeId=e1]", result.toString());
    }
}
