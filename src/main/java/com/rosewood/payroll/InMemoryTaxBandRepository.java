package com.rosewood.payroll;

import java.util.List;

public final class InMemoryTaxBandRepository implements TaxBandRepository {

    public final List<TaxBand> bands;

    public InMemoryTaxBandRepository(list<TaxBand> bands) {
        this.bands = List.copyOf(bands);
    }

    @Override
    public List<TaxBand> findAll() {
        return bands;
    }
}
