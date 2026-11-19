package com.rosewood.payroll.employee;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class InMemoryEmployeeRepositoryTest {

    private final Employee efua = new Employee("e1", "Efua Sarpong");
    private final Employee kwame = new Employee("e2", "Kwame Owusu");

    @Test
    void findsAnEmployeeById() {
        EmployeeRepository repository = new InMemoryEmployeeRepository(List.of(efua, kwame));

        assertEquals(Optional.of(kwame), repository.findById("e2"));
    }

    @Test
    void findsNothingForAnUnknownId() {
        EmployeeRepository repository = new InMemoryEmployeeRepository(List.of(efua));

        assertEquals(Optional.empty(), repository.findById("unknown"));
    }

    @Test
    void ignoresLaterChangesToTheSourceCollection() {
        List<Employee> source = new ArrayList<>(List.of(efua));
        EmployeeRepository repository = new InMemoryEmployeeRepository(source);

        source.add(kwame);

        assertEquals(Optional.empty(), repository.findById("e2"));
    }

    @Test
    void rejectsDuplicateIdsWithoutNamingTheEmployee() {
        Employee duplicate = new Employee("e1", "Ama Mensah");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new InMemoryEmployeeRepository(List.of(efua, duplicate)));
        assertEquals("Duplicate employee id: e1", exception.getMessage());
    }

    @Test
    void rejectsMissingEmployee() {
        List<Employee> withMissingEmployee = Arrays.asList(efua, null);

        assertThrows(NullPointerException.class,
                () -> new InMemoryEmployeeRepository(withMissingEmployee));
    }
}
