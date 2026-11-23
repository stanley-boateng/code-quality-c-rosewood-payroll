package com.rosewood.payroll.employee;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Holds a fixed set of employees in memory and finds them by id.
 */
public final class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> employeesById;

    /**
     * @throws IllegalArgumentException if two employees share an id
     */
    public InMemoryEmployeeRepository(Collection<Employee> employees) {
        Objects.requireNonNull(employees, "employees");
        Map<String, Employee> byId = new HashMap<>();
        for (Employee employee : employees) {
            Objects.requireNonNull(employee, "employees must not contain null");
            if (byId.putIfAbsent(employee.id(), employee) != null) {
                // Id only: names are personal data and must not appear in exception messages.
                throw new IllegalArgumentException("Duplicate employee id: " + employee.id());
            }
        }
        this.employeesById = Map.copyOf(byId);
    }

    @Override
    public Optional<Employee> findById(String id) {
        Objects.requireNonNull(id, "id");
        return Optional.ofNullable(employeesById.get(id));
    }
}
