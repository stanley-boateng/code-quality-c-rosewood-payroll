package com.rosewood.payroll.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class EmployeeTest {

    @ParameterizedTest(name = "id \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsBlankId(String id) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Employee(id, "Efua Sarpong"));
        assertEquals("Employee id must not be blank", exception.getMessage());
    }

    @ParameterizedTest(name = "name \"{0}\" is rejected")
    @NullAndEmptySource
    @ValueSource(strings = {"  "})
    void rejectsBlankName(String name) {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new Employee("e1", name));
        assertEquals("Employee name must not be blank", exception.getMessage());
    }

    @Test
    void showsOnlyTheIdWhenPrinted() {
        assertEquals("Employee[id=e1]", new Employee("e1", "Efua Sarpong").toString());
    }
}
