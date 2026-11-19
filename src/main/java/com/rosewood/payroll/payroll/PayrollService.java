package com.rosewood.payroll.payroll;

import com.rosewood.payroll.employee.Employee;
import com.rosewood.payroll.employee.EmployeeNotFoundException;
import com.rosewood.payroll.employee.EmployeeRepository;
import com.rosewood.payroll.grosspay.GrossPayPolicy;
import com.rosewood.payroll.grosspay.WorkPeriod;
import com.rosewood.payroll.tax.TaxBandConfigurationException;
import com.rosewood.payroll.tax.TaxPolicy;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Calculates an employee's gross pay, tax and net pay for a period, rounded to whole pesewas.
 */
public final class PayrollService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final EmployeeRepository employees;
    private final GrossPayPolicy grossPayPolicy;
    private final TaxPolicy taxPolicy;

    public PayrollService(
            EmployeeRepository employees, GrossPayPolicy grossPayPolicy, TaxPolicy taxPolicy) {
        this.employees = Objects.requireNonNull(employees, "employees");
        this.grossPayPolicy = Objects.requireNonNull(grossPayPolicy, "grossPayPolicy");
        this.taxPolicy = Objects.requireNonNull(taxPolicy, "taxPolicy");
    }

    /**
     * @throws EmployeeNotFoundException if no employee has this id
     * @throws TaxBandConfigurationException if the tax bands cannot price this gross pay
     */
    public PayResult calculatePay(String employeeId, WorkPeriod period) {
        Objects.requireNonNull(employeeId, "employeeId");
        Objects.requireNonNull(period, "period");

        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        BigDecimal gross = toMoney(grossPayPolicy.calculateGrossPay(period));
        BigDecimal tax = toMoney(taxPolicy.calculateTax(gross));
        BigDecimal net = gross.subtract(tax);

        return new PayResult(employee, gross, tax, net);
    }

    private static BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
