package com.rosewood.payroll;

public class InvalidWorkPeriodException extends PayrollException{

    public InvalidWorkPeriodException(String message) {
        super(message);
    }

    public InvalidWorkPeriodException(String message, Throwable cause) {
        super(message,cause);
    }
}
