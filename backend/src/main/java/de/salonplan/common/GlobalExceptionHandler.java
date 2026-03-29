package de.salonplan.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(
            NotFoundException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.NOT_FOUND,
                "NOT_FOUND",
                exception.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(BusinessConflictException.class)
    ResponseEntity<ApiError> handleBusinessConflict(
            BusinessConflictException exception,
            HttpServletRequest request
    ) {
        String code = exception.getCode();

        if (code == null || code.isBlank()) {
            code = "BUSINESS_CONFLICT";
        }

        return response(
                HttpStatus.CONFLICT,
                code,
                exception.getMessage(),
                request,
                Map.of()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.UNAUTHORIZED,
                "INVALID_CREDENTIALS",
                "E-Mail oder Passwort ist ungültig.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.FORBIDDEN,
                "ACCESS_DENIED",
                "Du besitzt nicht die erforderliche Berechtigung.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        for (FieldError fieldError
                : exception.getBindingResult().getFieldErrors()) {

            String message = fieldError.getDefaultMessage();

            fieldErrors.putIfAbsent(
                    fieldError.getField(),
                    message != null ? message : "Ungültiger Wert"
            );
        }

        return response(
                HttpStatus.BAD_REQUEST,
                "VALIDATION_FAILED",
                "Die übermittelten Daten sind ungültig.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        exception.getConstraintViolations().forEach(violation ->
                fieldErrors.putIfAbsent(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                )
        );

        return response(
                HttpStatus.BAD_REQUEST,
                "CONSTRAINT_VIOLATION",
                "Die übermittelten Daten sind ungültig.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadableMessage(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {
        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_REQUEST_BODY",
                "Der Request-Body ist ungültig oder konnte nicht gelesen werden.",
                request,
                Map.of()
        );
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParameter(
            MissingServletRequestParameterException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = Map.of(
                exception.getParameterName(),
                "Der Parameter ist erforderlich."
        );

        return response(
                HttpStatus.BAD_REQUEST,
                "MISSING_REQUEST_PARAMETER",
                "Ein erforderlicher Request-Parameter fehlt.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {
        Map<String, String> fieldErrors = Map.of(
                exception.getName(),
                "Der übermittelte Wert besitzt ein ungültiges Format."
        );

        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_PARAMETER_TYPE",
                "Ein Request-Parameter besitzt ein ungültiges Format.",
                request,
                fieldErrors
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ApiError> handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {
        String message = exception.getMessage();

        if (message == null || message.isBlank()) {
            message = "Der übermittelte Wert ist ungültig.";
        }

        return response(
                HttpStatus.BAD_REQUEST,
                "INVALID_ARGUMENT",
                message,
                request,
                Map.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();

        log.warn(
                "Database constraint violation. correlationId={}, path={}",
                correlationId,
                request.getRequestURI(),
                exception
        );

        return response(
                HttpStatus.CONFLICT,
                "DATA_INTEGRITY_VIOLATION",
                "Die Änderung konnte wegen eines Datenkonflikts nicht gespeichert werden. "
                        + "Referenz: " + correlationId,
                request,
                Map.of()
        );
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(
            Exception exception,
            HttpServletRequest request
    ) {
        String correlationId = UUID.randomUUID().toString();

        log.error(
                "Unexpected error. correlationId={}, method={}, path={}",
                correlationId,
                request.getMethod(),
                request.getRequestURI(),
                exception
        );

        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "INTERNAL_SERVER_ERROR",
                "Unerwarteter Serverfehler. Referenz: " + correlationId,
                request,
                Map.of()
        );
    }

    private ResponseEntity<ApiError> response(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request,
            Map<String, String> fieldErrors
    ) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                code,
                message,
                request.getRequestURI(),
                fieldErrors
        );

        return ResponseEntity
                .status(status)
                .body(body);
    }
}