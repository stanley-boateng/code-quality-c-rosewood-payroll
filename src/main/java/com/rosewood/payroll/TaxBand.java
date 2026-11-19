package com.rosewood.payroll;

import java.math.BigDecimal;

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
    BigDecimal taxFor(BigDecimal grossPay);
}
