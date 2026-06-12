package com.arbit.app.common.exception;

import com.arbit.app.common.response.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException exception,
                                                                     HttpServletRequest request) {
        ErrorCode errorCode = exception.getErrorCode();
        if (errorCode == ErrorCode.UNAUTHORIZED) {
            log.warn("Business exception returned 401. method={}, uri={}, authorizationHeaderPresent={}, message={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    request.getHeader("Authorization") != null,
                    exception.getMessage());
        }
        return json(errorCode.status().value(), ApiResponse.error(errorCode.name(), exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(FieldError::getField, Collectors.mapping(
                        FieldError::getDefaultMessage,
                        Collectors.joining(", ")
                )))
                .entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("; "));
        return json(ErrorCode.INVALID_REQUEST.status().value(),
                ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception) {
        Throwable cause = exception.getCause();
        if (cause instanceof InvalidFormatException invalidFormatException
                && invalidFormatException.getTargetType() != null
                && invalidFormatException.getTargetType().isEnum()) {
            String fieldName = invalidFormatException.getPath().isEmpty()
                    ? "unknown"
                    : invalidFormatException.getPath().get(invalidFormatException.getPath().size() - 1).getFieldName();
            String validValues = Arrays.stream(invalidFormatException.getTargetType().getEnumConstants())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            String message = fieldName + " has an invalid enum value. Allowed values: " + validValues;
            return json(ErrorCode.INVALID_REQUEST.status().value(),
                    ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), message));
        }

        return json(ErrorCode.INVALID_REQUEST.status().value(),
                ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), "Request body format is invalid."));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDeniedException() {
        return json(ErrorCode.FORBIDDEN.status().value(),
                ApiResponse.error(ErrorCode.FORBIDDEN.name(), ErrorCode.FORBIDDEN.message()));
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotAcceptableException() {
        return json(406, ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), "Accept header is not supported."));
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMediaTypeNotSupportedException() {
        return json(415,
                ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), "Content-Type header is not supported."));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpRequestMethodNotSupportedException() {
        return json(405, ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), "HTTP method is not supported."));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception) {
        String message = exception.getParameterName() + " parameter is required.";
        return json(ErrorCode.INVALID_REQUEST.status().value(),
                ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException exception) {
        String message = exception.getName() + " parameter has an invalid format.";
        return json(ErrorCode.INVALID_REQUEST.status().value(),
                ApiResponse.error(ErrorCode.INVALID_REQUEST.name(), message));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException() {
        return json(ErrorCode.NOT_FOUND.status().value(),
                ApiResponse.error(ErrorCode.NOT_FOUND.name(), ErrorCode.NOT_FOUND.message()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException() {
        return json(ErrorCode.INTERNAL_ERROR.status().value(),
                ApiResponse.error(ErrorCode.INTERNAL_ERROR.name(), ErrorCode.INTERNAL_ERROR.message()));
    }

    private ResponseEntity<ApiResponse<Void>> json(int status, ApiResponse<Void> body) {
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }
}
