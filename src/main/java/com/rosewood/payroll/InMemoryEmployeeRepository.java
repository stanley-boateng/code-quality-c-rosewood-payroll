package com.rosewood.payroll;

import java.util.Collection;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> employeesById;

    public InMemoryEmployeeRepository(Collection<Employee> employees) {
        this.employeesById = employees.stream()
                .collect(Collectors.toUnmodifiableMap(Employee::id, Function.identity()));
    }

    @Override
    public Optional<Employee> findById(String id) {
        return Optional.ofNullable(employeesById.get(id));
    }
}
