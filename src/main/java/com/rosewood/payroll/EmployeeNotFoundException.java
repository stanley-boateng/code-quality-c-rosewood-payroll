package com.rosewood.payroll;

public class EmployeeNotFoundException extends PayrollException {

    private final String employeeId;

    public EmployeeNotFoundException(String employeeId) {
        super("No employee found with id '" + employeeId + "'");
        this.employeeId = employeeId;
    }

    public String getEmployeeId() {
        return employeeId;
    }
}
