package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;

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
}
