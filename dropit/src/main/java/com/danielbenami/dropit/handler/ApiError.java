package com.danielbenami.dropit.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class ApiError {


    private final int status;
    private final String message;
    Map<String,String> validationErrors = new HashMap<>();
    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
