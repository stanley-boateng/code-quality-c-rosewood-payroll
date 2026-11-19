package com.rosewood.payroll.employee;

import com.rosewood.payroll.error.PayrollException;

/**
 * No employee exists with the requested id, so their pay cannot be calculated.
 */
public class EmployeeNotFoundException extends PayrollException {

    private static final long serialVersionUID = 1L;

    private final String employeeId;

    public EmployeeNotFoundException(String employeeId) {
        super("No employee found with id '" + employeeId + "'");
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}
