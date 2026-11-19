package com.rosewood.payroll;

import com.rosewood.payroll.employee.Employee;
import com.rosewood.payroll.employee.EmployeeRepository;
import com.rosewood.payroll.employee.InMemoryEmployeeRepository;
import com.rosewood.payroll.error.PayrollException;
import com.rosewood.payroll.grosspay.GrossPayPolicy;
import com.rosewood.payroll.grosspay.OvertimeGrossPayPolicy;
import com.rosewood.payroll.grosspay.WorkPeriod;
import com.rosewood.payroll.payroll.PayResult;
import com.rosewood.payroll.payroll.PayrollService;
import com.rosewood.payroll.tax.InMemoryTaxBandRepository;
import com.rosewood.payroll.tax.RateTaxBand;
import com.rosewood.payroll.tax.TaxBandRepository;
import com.rosewood.payroll.tax.TaxCalculator;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wires the payroll components together (the composition root) and runs a few examples.
 * This is the only class that knows which concrete implementations and settings are used.
 */
public class Demo {

    private static final BigDecimal STANDARD_HOURS_PER_PERIOD = new BigDecimal("40");
    private static final BigDecimal TIME_AND_A_HALF_PREMIUM = new BigDecimal("0.5");

    public static void main(String[] args) {
        PayrollService payroll = createPayrollService();

        printPay(payroll, "e1", "40", "20");
        printPay(payroll, "e2", "50", "30");
        printPay(payroll, "unknown", "40", "20");
        printPay(payroll, "e1", "-5", "20");
    }

    private static PayrollService createPayrollService() {
        EmployeeRepository employees = new InMemoryEmployeeRepository(List.of(
                new Employee("e1", "Efua Sarpong"),
                new Employee("e2", "Kwame Owusu")));

        GrossPayPolicy grossPayPolicy =
                new OvertimeGrossPayPolicy(STANDARD_HOURS_PER_PERIOD, TIME_AND_A_HALF_PREMIUM);

        TaxBandRepository taxBands = new InMemoryTaxBandRepository(List.of(
                RateTaxBand.between("LOW", "0", "1000", "0.05"),
                RateTaxBand.between("MEDIUM", "1000", "5000", "0.15"),
                RateTaxBand.from("HIGH", "5000", "0.25")));

        return new PayrollService(employees, grossPayPolicy, new TaxCalculator(taxBands));
    }

    private static void printPay(
            PayrollService payroll, String employeeId, String hours, String rate) {
        try {
            PayResult result = payroll.calculatePay(employeeId, WorkPeriod.of(hours, rate));
            System.out.printf("%s (%s): gross GHS %s, tax GHS %s, net GHS %s%n",
                    result.employee().name(), employeeId,
                    result.grossPay(), result.tax(), result.netPay());
        } catch (PayrollException exception) {
            System.out.println(
                    "Pay NOT calculated for " + employeeId + ": " + exception.getMessage());
        }
    }
}
