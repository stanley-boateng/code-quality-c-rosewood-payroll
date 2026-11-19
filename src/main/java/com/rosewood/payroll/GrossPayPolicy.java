package com.rosewood.payroll;

import java.math.BigDecimal;

public interface GrossPayPolicy {

    BigDecimal grossPay(WorkPeriod period);
}
