package com.rosewood.payroll.tax;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class InMemoryTaxBandRepositoryTest {

    private final TaxBand low = RateTaxBand.between("LOW", "0", "1000", "0.05");
    private final TaxBand high = RateTaxBand.from("HIGH", "1000", "0.25");

    @Test
    void returnsTheBandsItWasGivenInOrder() {
        TaxBandRepository repository = new InMemoryTaxBandRepository(List.of(low, high));

        assertEquals(List.of(low, high), repository.findAll());
    }

    @Test
    void ignoresLaterChangesToTheSourceList() {
        List<TaxBand> source = new ArrayList<>(List.of(low));
        TaxBandRepository repository = new InMemoryTaxBandRepository(source);

        source.add(high);

        assertEquals(List.of(low), repository.findAll());
    }
}
