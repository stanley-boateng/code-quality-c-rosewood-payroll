package com.rosewood.payroll;

/** An employee on the payroll. The id and name are always present. */
public record Employee(String id, String name) {

    public Employee {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Employee id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name must not be blank");
        }
    }

    /** Id only, so logging or printing an Employee never exposes their name. */
    @Override
    public String toString() {
        return "Employee[id=" + id + "]";
    }
}