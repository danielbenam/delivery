package com.danielbenami.dropit.handler;

import com.danielbenami.dropit.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


@RestControllerAdvice
public class ApplicationExceptionHandler {


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiError handleInvalidArgument(MethodArgumentNotValidException ex){
        ApiError errorMessage = new ApiError(400, ex.getMessage());
        Map<String,String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error->
                errorMap.put(error.getField(), error.getDefaultMessage()));

        errorMessage.setValidationErrors(errorMap);
        return errorMessage;
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({AddressNotSupportedByTimeslotException.class, AddressAlreadyAssignedToTimeslotException.class, TimeslotNotFoundException.class, AddressNotFoundException.class,
            DeliveryNotFoundException.class ,DeliveryWasAlreadyCanceledException.class, DeliveryWasAlreadyCompletedException.class})
    public ApiError handlePaymentNotFoundException(Exception ex) {
        ApiError errorMessage = new ApiError(400, ex.getMessage());
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("errorMessage", ex.getMessage());
        return errorMessage;
    }
}
