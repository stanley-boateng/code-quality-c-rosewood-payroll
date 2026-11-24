package com.rosewood.payroll.tax;

import java.util.List;
import java.util.Objects;

/**
 * Holds a fixed list of tax bands in memory.
 */
public final class InMemoryTaxBandRepository implements TaxBandRepository {

    private final List<TaxBand> bands;

    public InMemoryTaxBandRepository(List<TaxBand> bands) {
        Objects.requireNonNull(bands, "bands");
        if (bands.stream().anyMatch(Objects::isNull)) {
            throw new NullPointerException("bands must not contain null");
        }
        this.bands = List.copyOf(bands);
    }

    @Override
    public List<TaxBand> findAll() {
        return bands;
    }
}
