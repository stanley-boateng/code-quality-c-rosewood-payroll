package com.rosewood.payroll.tax;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Taxes gross pay using the one configured band that covers it.
 */
public final class TaxCalculator implements TaxPolicy {

    private final TaxBandRepository taxBands;

    public TaxCalculator(TaxBandRepository taxBands) {
        this.taxBands = Objects.requireNonNull(taxBands, "taxBands");
    }

    /**
     * @throws TaxBandConfigurationException if no band or more than one band covers this
     *     gross pay, or the chosen band returns a tax outside 0..grossPay
     */
    @Override
    public BigDecimal calculateTax(BigDecimal grossPay) {
        Objects.requireNonNull(grossPay, "grossPay");
        TaxBand band = findOnlyBandCovering(grossPay);
        return requireTaxWithinGross(band, band.calculateTax(grossPay), grossPay);
    }

    private TaxBand findOnlyBandCovering(BigDecimal grossPay) {
        List<TaxBand> matching = taxBands.findAll().stream()
                .filter(band -> band.appliesTo(grossPay))
                .toList();

        if (matching.isEmpty()) {
            throw new TaxBandConfigurationException("No tax band covers gross pay " + grossPay);
        }
        if (matching.size() > 1) {
            String names = matching.stream().map(TaxBand::name).collect(Collectors.joining(", "));
            throw new TaxBandConfigurationException(
                    "Gross pay " + grossPay + " is covered by more than one tax band: " + names);
        }
        return matching.get(0);
    }

    private static BigDecimal requireTaxWithinGross(
            TaxBand band, BigDecimal tax, BigDecimal grossPay) {
        if (tax == null || tax.signum() < 0 || tax.compareTo(grossPay) > 0) {
            throw new TaxBandConfigurationException("Tax band '" + band.name() + "' returned "
                    + tax + " for gross pay " + grossPay);
        }
        return tax;
    }
}
