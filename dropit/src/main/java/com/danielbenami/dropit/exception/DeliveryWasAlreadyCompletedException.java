package com.danielbenami.dropit.exception;

public class DeliveryWasAlreadyCompletedException extends RuntimeException{

    public DeliveryWasAlreadyCompletedException(String message){
        super(message);
    }
}
