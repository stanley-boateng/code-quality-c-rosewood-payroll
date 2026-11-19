package com.rosewood.payroll.grosspay;

import java.math.BigDecimal;

/**
 * Works out gross pay (before tax) for a period of work.
 */
public interface GrossPayPolicy {

    /**
     * @return gross pay for the period; never negative.
     */
    BigDecimal calculateGrossPay(WorkPeriod period);
}
