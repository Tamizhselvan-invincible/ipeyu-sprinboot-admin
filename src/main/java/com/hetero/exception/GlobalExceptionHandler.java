package com.hetero.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hetero.utils.ApiErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;


import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);

    // Handle NoHandlerFoundException (Invalid URL)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException exception) {
        log.error("Handler not found: {}", exception.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", exception.getMessage(), null);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<?> handleJsonProcessingExceptionException(JsonProcessingException exception) {
        log.error("Json Not Processing Exception : {}", exception.getMessage());

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Error", exception.getMessage(), null);
    }

    // Handle NoResourceFoundException (Custom Not Found Exception)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException exception) {
        log.error("Resource not found: {}", exception.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", exception.getMessage(), null);
    }

    // Handle UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<?> handleUserNotFoundException(UserNotFoundException exception) {
        log.error("User not found: {}", exception.getMessage());

        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", exception.getMessage(), null);
    }

    // Handle ConstraintViolationException (Validation Errors)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", errors);
    }

    // Handle MethodArgumentNotValidException (Validation Errors in Request Body)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse<Map<String, String>>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", "Request validation failed", errors);
    }

    @ExceptionHandler(InvalidException.class)
    public ResponseEntity<?> handleInValidException(InvalidException ex) {
        log.error("Unexpected error: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Invalid Data", ex.getMessage(), null);
    }

    // Handle General Exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneralException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage(), null);
    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignatureException(SignatureException ex) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED,"Invalid JWT token", ex.getMessage(), null);
    }


    @ExceptionHandler(JwtException.class)
    public ResponseEntity<?> handleJwtException(JwtException ex) {
        log.error("JWT Exception: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "Authentication Failed", ex.getMessage(), null);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleJwtException(ExpiredJwtException ex) {
        log.error("JWT Token is Expired Exception: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "JWT Token is Expired", ex.getMessage(), null);
    }

    @ExceptionHandler(JWTTokenNotValid.class)
    public ResponseEntity<?> handleJwtException(JWTTokenNotValid ex) {
        log.error("JWT Token is Might Be Expired or Reset Exception: {}", ex.getMessage());

        return buildErrorResponse(HttpStatus.UNAUTHORIZED, "JWT Token is Might be Expired or Reset", ex.getMessage(), null);
    }

    // Common method to build standard error response
    private ResponseEntity<?> buildErrorResponse(HttpStatus status, String message, String error, Object data) {
        ApiErrorResponse<Object> response = new ApiErrorResponse<>(status.value(), message, error, data);
        return new ResponseEntity<>(response, status);
    }

    // Common method to build validation error response
    private ResponseEntity<ApiErrorResponse<Map<String, String>>> buildValidationErrorResponse(
            HttpStatus status, String message, String error, Map<String, String> errors) {

        ApiErrorResponse<Map<String, String>> response = new ApiErrorResponse<>(status.value(), message, error, errors);
        return new ResponseEntity<>(response, status);
    }
}

//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//
//    private static final Logger log = LogManager.getLogger(GlobalExceptionHandler.class);
//
//    @ExceptionHandler(NoHandlerFoundException.class)
//    public ResponseEntity<?> handleNoHandlerFoundException(NoHandlerFoundException exception) {
//        log.error("Handler not found", exception.getMessage());
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", 404);
//        response.put("error", "Not Found");
//        response.put("message", exception.getMessage());
//        response.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    @ExceptionHandler(NoResourceFoundException.class)
//    public ResponseEntity<?> handleNoResourceFoundException(NoResourceFoundException exception) {
//        log.error("Resource not found", exception.getMessage());
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", 404);
//        response.put("error", "Not Found");
//        response.put("message", exception.getMessage());
//        response.put("timestamp", LocalDateTime.now());
//
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
//    }
//
//    // Handle UserNotFoundException
//    @ExceptionHandler(UserNotFoundException.class)
//    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(UserNotFoundException ex) {
//        return buildErrorResponse(HttpStatus.NOT_FOUND, "User Not Found", ex.getMessage());
//    }
//
//    // Handle ConstraintViolationException (Validation Errors)
//    @ExceptionHandler(ConstraintViolationException.class)
//    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException ex) {
//        Map<String, String> errors = ex.getConstraintViolations().stream()
//                .collect(Collectors.toMap(
//                        v -> v.getPropertyPath().toString(),  // Field name
//                        ConstraintViolation::getMessage       // Custom error message
//                ));
//
//        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
//    }
//
//    // Handle MethodArgumentNotValidException (Validation Errors in Request Body)
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
//        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
//                .collect(Collectors.toMap(
//                        FieldError::getField,  // Field name
//                        FieldError::getDefaultMessage  // Custom error message
//                ));
//
//        return buildValidationErrorResponse(HttpStatus.BAD_REQUEST, "Validation Failed", errors);
//    }
//
//    // Handle General Exceptions
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex) {
//        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", ex.getMessage());
//    }
//
//    // Common method to build minimal error response
//    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", status.value());
//        response.put("error", error);
//        response.put("message", message);
//        response.put("timestamp", LocalDateTime.now());
//
//        return new ResponseEntity<>(response, status);
//    }
//
//    // Common method to build validation error response
//    private ResponseEntity<Map<String, Object>> buildValidationErrorResponse(HttpStatus status, String error, Map<String, String> errors) {
//        Map<String, Object> response = new HashMap<>();
//        response.put("status", status.value());
//        response.put("error", error);
//        response.put("errors", errors); // Only field names and messages
//        response.put("timestamp", LocalDateTime.now());
//
//        return new ResponseEntity<>(response, status);
//    }
//}

