package com.pathfinder.summons.web;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = DailyUsesController.class)
public class DailyUsesExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class})
    public ResponseEntity<ErrorResponse> handleInvalidDailyUses(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("INVALID_DAILY_USES", exception.getMessage(), Map.of()));
    }

    public record ErrorResponse(String code, String message, Map<String, Object> details) {
    }
}
