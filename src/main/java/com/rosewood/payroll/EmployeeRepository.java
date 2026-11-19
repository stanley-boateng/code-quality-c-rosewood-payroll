package com.rosewood.payroll;

import java.util.Optional;

/**
 * Finds employees by id.
 */
public interface EmployeeRepository {

    /**
     * @param id the employee id; must not be null
     * @return the employee with this id, or {@link Optional#empty()} if there is none. Never null.
     */
    Optional<Employee> findById(String id);
}