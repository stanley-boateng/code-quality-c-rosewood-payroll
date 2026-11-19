package com.rosewood.payroll.tax;

import java.math.BigDecimal;

/**
 * One band of the tax table: the gross pay it covers and how it taxes that pay.
 */
public interface TaxBand {

    /**
     * Short name for messages, e.g. "LOW".
     */
    String name();

    /**
     * @return true if this band covers this gross pay.
     */
    boolean appliesTo(BigDecimal grossPay);

    /**
     * @return tax due on this gross pay under this band's rules; between 0 and grossPay.
     */
    BigDecimal calculateTax(BigDecimal grossPay);
}
