package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * The outcome of a pay calculation: who it is for, and the amounts on their payslip.
 */
public record PayResult(Employee employee, BigDecimal grossPay, BigDecimal tax, BigDecimal netPay) {

    public PayResult {
        Objects.requireNonNull(employee, "employee");
        Objects.requireNonNull(grossPay, "grossPay");
        Objects.requireNonNull(tax, "tax");
        Objects.requireNonNull(netPay, "netPay");
        if (grossPay.subtract(tax).compareTo(netPay) != 0) {
            throw new IllegalArgumentException(
                    "Payslip does not add up: gross " + grossPay + " - tax " + tax + " != net " + netPay);
        }
    }

    /**
     * Employee id only: names and pay amounts are personal data and stay out of logs.
     */
    @Override
    public String toString() {
        return "PayResult[employeeId=" + employee.id() + "]";
    }
}