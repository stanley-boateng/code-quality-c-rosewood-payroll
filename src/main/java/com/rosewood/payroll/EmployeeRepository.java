package com.rosewood.payroll;

import java.util.Optional;

public interface EmployeeRepository {
    /** @return the employee with this id, or Optional.empty() if there is none.
    Never null. */
    Optional<Employee> findById(String id);
}