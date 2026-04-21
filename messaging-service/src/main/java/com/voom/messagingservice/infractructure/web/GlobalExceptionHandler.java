package com.voom.messagingservice.infractructure.web;

import com.voom.messagingservice.domain.exception.DecryptionException;
import com.voom.messagingservice.domain.exception.MessageNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MessageNotFoundException.class)
    public ResponseEntity<Object> handleMessageNotFoundException(MessageNotFoundException e) {
        return new ResponseEntity<>(
                Map.of("context", "Error while fetching message",
                            "message", e.getMessage())
                , HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DecryptionException.class)
    public ResponseEntity<Object> handleDecryptionException(DecryptionException e) {
        return new ResponseEntity<>(
                Map.of("context", "Error while fetching message",
                        "message", e.getMessage())
                , HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        Map<String, String> fields = e.getFieldErrors().stream().collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage));
        return new ResponseEntity<>(
                Map.of(
                        "context", "Error while processing request",
                        "message", fields
                ),
                HttpStatus.BAD_REQUEST
                );
    }
}
