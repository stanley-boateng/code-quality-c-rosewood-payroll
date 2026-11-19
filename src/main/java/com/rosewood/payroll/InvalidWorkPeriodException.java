package com.rosewood.payroll;

/**
 * The hours or rate supplied for a pay period are not valid, so pay cannot be calculated.
 */
public class InvalidWorkPeriodException extends PayrollException {

    private static final long serialVersionUID = 1L;

    public InvalidWorkPeriodException(String message) {
        super(message);
    }

    public InvalidWorkPeriodException(String message, Throwable cause) {
        super(message, cause);
    }
}