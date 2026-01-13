package org.example.projectjee.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("message", ex.getMessage());

        // Determine HTTP status based on message content
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        
        String message = ex.getMessage();
        if (message != null) {
            if (message.contains("introuvable") || message.contains("not found")) {
                status = HttpStatus.NOT_FOUND;
            } else if (message.contains("doivent être") || message.contains("ne peut pas") || 
                       message.contains("validation") || message.contains("invalid")) {
                status = HttpStatus.BAD_REQUEST;
            }
        }

        body.put("status", status.value());
        return new ResponseEntity<>(body, status);
    }
}
