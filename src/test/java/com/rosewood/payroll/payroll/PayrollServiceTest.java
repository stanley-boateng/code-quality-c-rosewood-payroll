package com.rosewood.payroll.payroll;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.rosewood.payroll.employee.Employee;
import com.rosewood.payroll.employee.EmployeeNotFoundException;
import com.rosewood.payroll.employee.InMemoryEmployeeRepository;
import com.rosewood.payroll.grosspay.GrossPayPolicy;
import com.rosewood.payroll.grosspay.WorkPeriod;
import com.rosewood.payroll.tax.TaxPolicy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class PayrollServiceTest {

    private static final WorkPeriod ANY_PERIOD = WorkPeriod.of("40", "20");

    private final Employee efua = new Employee("e1", "Efua Sarpong");

    @Test
    void paysGrossMinusTaxToTheEmployee() {
        PayrollService service = serviceWith(period -> new BigDecimal("1000"),
                gross -> new BigDecimal("150"));

        PayResult result = service.calculatePay("e1", ANY_PERIOD);

        assertAll(
                () -> assertSame(efua, result.employee()),
                () -> assertEquals(new BigDecimal("1000.00"), result.grossPay()),
                () -> assertEquals(new BigDecimal("150.00"), result.tax()),
                () -> assertEquals(new BigDecimal("850.00"), result.netPay()));
    }

    @Test
    void roundsGrossAndTaxHalfUpToTwoDecimalPlaces() {
        PayrollService service = serviceWith(period -> new BigDecimal("1234.565"),
                gross -> new BigDecimal("49.9945"));

        PayResult result = service.calculatePay("e1", ANY_PERIOD);

        assertAll(
                () -> assertEquals(new BigDecimal("1234.57"), result.grossPay()),
                () -> assertEquals(new BigDecimal("49.99"), result.tax()),
                () -> assertEquals(new BigDecimal("1184.58"), result.netPay()));
    }

    @Test
    void taxesTheRoundedGrossPay() {
        List<BigDecimal> taxedAmounts = new ArrayList<>();
        PayrollService service = serviceWith(period -> new BigDecimal("100.005"), gross -> {
            taxedAmounts.add(gross);
            return BigDecimal.ZERO;
        });

        service.calculatePay("e1", ANY_PERIOD);

        assertEquals(List.of(new BigDecimal("100.01")), taxedAmounts);
    }

    @Test
    void rejectsUnknownEmployee() {
        PayrollService service = serviceWith(period -> BigDecimal.ONE, gross -> BigDecimal.ZERO);

        EmployeeNotFoundException exception = assertThrows(EmployeeNotFoundException.class,
                () -> service.calculatePay("unknown", ANY_PERIOD));
        assertAll(
                () -> assertEquals("unknown", exception.getEmployeeId()),
                () -> assertEquals("No employee found with id 'unknown'",
                        exception.getMessage()));
    }

    private PayrollService serviceWith(GrossPayPolicy grossPayPolicy, TaxPolicy taxPolicy) {
        return new PayrollService(
                new InMemoryEmployeeRepository(List.of(efua)), grossPayPolicy, taxPolicy);
    }
}
