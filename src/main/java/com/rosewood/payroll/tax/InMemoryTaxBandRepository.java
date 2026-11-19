package com.rosewood.payroll.tax;

import java.util.List;

/**
 * Holds a fixed list of tax bands in memory.
 */
public final class InMemoryTaxBandRepository implements TaxBandRepository {

    private final List<TaxBand> bands;

    public InMemoryTaxBandRepository(List<TaxBand> bands) {
        this.bands = List.copyOf(bands);
    }

    @Override
    public List<TaxBand> findAll() {
        return bands;
    }
}
