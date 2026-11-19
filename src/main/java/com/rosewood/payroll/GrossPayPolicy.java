package com.rosewood.payroll;

import java.math.BigDecimal;

/**
 * Works out gross pay (before tax) for a period of work.
 */
public interface GrossPayPolicy {

    /**
     * @return gross pay for the period; never negative.
     */
    BigDecimal grossPay(WorkPeriod period);
}