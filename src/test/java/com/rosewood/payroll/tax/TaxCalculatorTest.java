package com.rosewood.payroll.tax;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static com.rosewood.payroll.MoneyAssertions.assertAmount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxCalculatorTest {

    private static final BigDecimal GROSS = new BigDecimal("2000.00");

    @Mock
    private TaxBand band;

    @ParameterizedTest(name = "gross {0} -> tax {1}")
    @CsvSource({
            "0.00, 0",
            "999.99, 49.9995",
            "1000.00, 150",
            "4999.99, 749.9985",
            "5000.00, 1250",
            "100000.00, 25000"
    })
    void appliesTheRateOfTheBandCoveringTheGrossPay(String gross, String expectedTax) {
        assertAmount(expectedTax, rosewoodBands().calculateTax(new BigDecimal(gross)));
    }

    @Test
    void acceptsAnyTaxBandImplementation() {
        when(band.appliesTo(GROSS)).thenReturn(true);
        when(band.calculateTax(GROSS)).thenReturn(new BigDecimal("123.45"));

        assertAmount("123.45", calculatorWith(band).calculateTax(GROSS));
    }

    @ParameterizedTest(name = "tax {0} on gross 2000.00 is allowed")
    @CsvSource({"0", "2000.00"})
    void allowsTaxFromZeroUpToTheWholeGross(String tax) {
        when(band.appliesTo(GROSS)).thenReturn(true);
        when(band.calculateTax(GROSS)).thenReturn(new BigDecimal(tax));

        assertAmount(tax, calculatorWith(band).calculateTax(GROSS));
    }

    @Test
    void rejectsGrossPayCoveredByNoBand() {
        TaxCalculator calculator = calculatorWith(
                RateTaxBand.between("LOW", "0", "1000", "0.05"),
                RateTaxBand.from("HIGH", "2000", "0.25"));

        TaxBandConfigurationException exception = assertThrows(
                TaxBandConfigurationException.class,
                () -> calculator.calculateTax(new BigDecimal("1500.00")));
        assertEquals("No tax band covers gross pay 1500.00", exception.getMessage());
    }

    @Test
    void rejectsGrossPayCoveredByMoreThanOneBand() {
        TaxCalculator calculator = calculatorWith(
                RateTaxBand.from("HIGH", "5000", "0.25"),
                RateTaxBand.from("TOP", "10000", "0.30"));

        TaxBandConfigurationException exception = assertThrows(
                TaxBandConfigurationException.class,
                () -> calculator.calculateTax(new BigDecimal("12000.00")));
        assertEquals("Gross pay 12000.00 is covered by more than one tax band: HIGH, TOP",
                exception.getMessage());
    }

    @ParameterizedTest(name = "tax {0} on gross 2000.00 is rejected")
    @CsvSource({"-0.01", "2000.01"})
    void rejectsTaxOutsideZeroToGross(String tax) {
        when(band.appliesTo(GROSS)).thenReturn(true);
        when(band.calculateTax(GROSS)).thenReturn(new BigDecimal(tax));
        when(band.name()).thenReturn("BROKEN");

        TaxBandConfigurationException exception = assertThrows(
                TaxBandConfigurationException.class,
                () -> calculatorWith(band).calculateTax(GROSS));
        assertEquals("Tax band 'BROKEN' returned " + tax + " for gross pay 2000.00",
                exception.getMessage());
    }

    @Test
    void rejectsNullTaxFromABand() {
        when(band.appliesTo(GROSS)).thenReturn(true);
        when(band.calculateTax(GROSS)).thenReturn(null);
        when(band.name()).thenReturn("BROKEN");

        TaxBandConfigurationException exception = assertThrows(
                TaxBandConfigurationException.class,
                () -> calculatorWith(band).calculateTax(GROSS));
        assertTrue(exception.getMessage().startsWith("Tax band 'BROKEN' returned null"));
    }

    @Test
    void rejectsNullGrossPay() {
        assertThrows(NullPointerException.class, () -> rosewoodBands().calculateTax(null));
    }

    @Test
    void rejectsNullRepository() {
        assertThrows(NullPointerException.class, () -> new TaxCalculator(null));
    }

    private static TaxCalculator rosewoodBands() {
        return calculatorWith(
                RateTaxBand.between("LOW", "0", "1000", "0.05"),
                RateTaxBand.between("MEDIUM", "1000", "5000", "0.15"),
                RateTaxBand.from("HIGH", "5000", "0.25"));
    }

    private static TaxCalculator calculatorWith(TaxBand... bands) {
        return new TaxCalculator(new InMemoryTaxBandRepository(List.of(bands)));
    }
}
