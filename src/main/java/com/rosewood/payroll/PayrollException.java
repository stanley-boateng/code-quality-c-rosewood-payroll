package com.rosewood.payroll;

public abstract class PayrollException extends RuntimeException {
    protected PayrollException(String message) {
        super(message);
    }

    protected  PayrollException(String message, Throwable cause) {
        super(message, cause);
    }
}
