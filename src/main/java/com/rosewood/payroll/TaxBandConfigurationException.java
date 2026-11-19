package com.rosewood.payroll;

/**
 * The configured tax bands cannot produce a trustworthy tax amount for a gross pay.
 */
public class TaxBandConfigurationException extends PayrollException {

    private static final long serialVersionUID = 1L;

    public TaxBandConfigurationException(String message) {
        super(message);
    }
}