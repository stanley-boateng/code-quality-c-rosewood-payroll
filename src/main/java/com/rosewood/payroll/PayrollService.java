package com.rosewood.payroll;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class PayrollService {

    private static final int MONEY_SCALE = 2;
    private static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final EmployeeRepository employees;
    private final GrossPayPolicy grossPayPolicy;
    private final TaxCalculator taxCalculator;

    public PayrollService(EmployeeRepository employees, GrossPayPolicy grossPayPolicy, TaxCalculator taxCalculator) {
        this.employees = Objects.requireNonNull(employees, "employees");
        this.grossPayPolicy = Objects.requireNonNull(grossPayPolicy, "grossPayPolicy");
        this.taxCalculator = Objects.requireNonNull(taxCalculator, "taxCalculator");
    }

    /**
     * @throws EmployeeNotFoundException     if no employee has this id
     * @throws TaxBandConfigurationException if the tax bands cannot price this gross pay
     */
    public PayResult calculatePay(String employeeId, WorkPeriod period) {
        Objects.requireNonNull(employeeId, "employeeId");
        Objects.requireNonNull(period, "period");

        Employee employee = employees.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));

        BigDecimal gross = toMoney(grossPayPolicy.grossPay(period));
        BigDecimal tax = toMoney(taxCalculator.taxFor(gross));
        BigDecimal net = gross.subtract(tax);

        return new PayResult(employee, gross, tax, net);
    }

    private static BigDecimal toMoney(BigDecimal amount) {
        return amount.setScale(MONEY_SCALE, MONEY_ROUNDING);
    }
}
