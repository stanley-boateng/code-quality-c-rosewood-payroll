package com.rosewood.payroll;

public record Employee(String id, String name) {
    public Employee {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Employee id must not be blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Employee name must not be blank");
        }
    }
}
