package com.yladraoui.ai.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * @author $ {USER}
 **/
public class GlobalExceptionHandler {
    public ResponseEntity<ApiError> handleNotFound(
            ConversationNotFoundException ex,
            HttpServletRequest request){
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request){
        ApiError body = ApiError.of(status.value(), status.getReasonPhrase(),message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
