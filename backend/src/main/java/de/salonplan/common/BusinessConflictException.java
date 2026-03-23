package de.salonplan.common;

public class BusinessConflictException extends RuntimeException {

    private final String code;

    public BusinessConflictException(String message) {
        this("BUSINESS_CONFLICT", message);
    }

    public BusinessConflictException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}