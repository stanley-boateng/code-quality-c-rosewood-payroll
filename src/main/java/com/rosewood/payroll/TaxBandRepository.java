package com.rosewood.payroll;

import java.util.List;

/**
 * Supplies the tax bands currently in force.
 */
public interface TaxBandRepository {

    /**
     * @return all configured bands; never null.
     */
    List<TaxBand> findAll();
}