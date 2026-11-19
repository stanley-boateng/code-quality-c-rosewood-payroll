package com.rosewood.payroll;

/**
 * Base type for failures in calculating pay. Callers that process many employees
 * can catch this one type, report the failure for that employee, and carry on.
 */
public abstract class PayrollException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    protected PayrollException(String message) {
        super(message);
    }

    protected PayrollException(String message, Throwable cause) {
        super(message, cause);
    }
}