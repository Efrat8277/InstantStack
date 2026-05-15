package com.example.instantstack.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // טיפול במקרה שמשאב (פרויקט או סביבה) לא נמצא
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String,String>> HandleException(RuntimeException ex){
        Map<String,String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    // טיפול כללי בשגיאות לא צפויות -מונע שגיאת 500 גנרית
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> HandleException(Exception ex){
        Map<String,String> error = new HashMap<>();
        error.put("message","server error, please try again/later");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(AuthException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error); // מחזיר 401
    }
}
