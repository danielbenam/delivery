package com.danielbenami.dropit.exception;

public class DeliveryWasAlreadyCanceledException extends RuntimeException{

    public DeliveryWasAlreadyCanceledException(String message){
        super(message);
    }
}
