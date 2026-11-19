package com.rosewood.payroll;

import java.math.BigDecimal;
import java.util.Objects;

public final class OvertimeGrossPayPolicy implements GrossPayPolicy {

    private final BigDecimal standardHours;
    private final BigDecimal overtimePremium;

    public OvertimeGrossPayPolicy(BigDecimal standardHours, BigDecimal overtimePremium) {
        this.standardHours = Objects.requireNonNull(standardHours, "standardHours");
        this.overtimePremium = Objects.requireNonNull(overtimePremium, "overtimePremium");
        if (standardHours.signum() <= 0) {
            throw new IllegalArgumentException("Standard hours must be greater than zero: " + standardHours);
        }
        if (overtimePremium.signum() < 0) {
            throw new IllegalArgumentException("Overtime premium must not be negative: " + overtimePremium);
        }
    }

    @Override
    public BigDecimal grossPay(WorkPeriod period) {
        BigDecimal basePay = period.hoursWorked().multiply(period.hourlyRate());
        BigDecimal overtimeHours = period.hoursWorked().subtract(standardHours).max(BigDecimal.ZERO);
        BigDecimal overtimePay = overtimeHours.multiply(period.hourlyRate()).multiply(overtimePremium);
        return basePay.add(overtimePay);
    }
}