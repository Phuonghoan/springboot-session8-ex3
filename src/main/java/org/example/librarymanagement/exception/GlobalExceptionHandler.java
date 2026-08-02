package org.example.librarymanagement.exception;

import org.example.librarymanagement.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleResourceNotFoundException(
            ResourceNotFoundException exception
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse>
    handleValidationException(
            MethodArgumentNotValidException exception
    ) {
        String validationMessage =
                exception.getBindingResult()
                        .getFieldErrors()
                        .stream()
                        .map(error ->
                                error.getField()
                                        + ": "
                                        + error.getDefaultMessage()
                        )
                        .distinct()
                        .collect(Collectors.joining("; "));

        if (validationMessage.isBlank()) {
            validationMessage = "Dữ liệu không hợp lệ";
        }

        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                validationMessage,
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse>
    handleIllegalArgumentException(
            IllegalArgumentException exception
    ) {
        ErrorResponse response = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage(),
                LocalDateTime.now()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}
