package com.soundboard.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.exc.UnrecognizedPropertyException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ValidationExceptionHandler {
  
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(
            (error) -> {
              String fieldName = ((FieldError) error).getField();
              String errorMessage = error.getDefaultMessage();
              errors.put(fieldName, errorMessage);
            }
    );
    return errors;
  }
  
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public Map<String, String> handleNotReadableJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
    Map<String, String> errors = new HashMap<>();
    Throwable cause = ex.getMostSpecificCause();
    if (cause instanceof UnrecognizedPropertyException unrecognizedPropertyException) {
      String badField = unrecognizedPropertyException.getPropertyName();
      
      errors.put("Field causing the issue", badField);
      return errors;
    }
    errors.put("Field causing the issue", "Undetermined, JSON is malformed");
    return  errors;
  }
  
}
