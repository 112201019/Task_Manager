package com.projects.task_manager.exception;
import com.projects.task_manager.dto.ErrorResponseDto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(
            EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        ErrorResponseDto errorDetails = new ErrorResponseDto(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "Resource Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGlobalException(
            Exception ex, HttpServletRequest request) {
        String correlationId = UUID.randomUUID().toString();

        log.error("Unhandled Exception [Correlation ID: {}] at {}", correlationId, request.getRequestURI(), ex);
        ErrorResponseDto errorDetails = new ErrorResponseDto(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please contact support with Error ID: " + correlationId,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        log.warn("Bad Request: {}", ex.getMessage());
        ErrorResponseDto errorDetails = new ErrorResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {

        List<String> errors = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.add(error.getField() + ": " + error.getDefaultMessage());
        }
        String combinedErrors = String.join(", ", errors);
        ErrorResponseDto errorDetails = new ErrorResponseDto(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                combinedErrors,
                request.getRequestURI()
        );
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
}