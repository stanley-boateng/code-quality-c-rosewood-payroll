package com.rosewood.payroll.tax;

import java.math.BigDecimal;

/**
 * Works out the tax due on a gross pay amount.
 */
public interface TaxPolicy {

    /**
     * @return tax due on this gross pay; between 0 and grossPay, not yet rounded.
     */
    BigDecimal calculateTax(BigDecimal grossPay);
}
