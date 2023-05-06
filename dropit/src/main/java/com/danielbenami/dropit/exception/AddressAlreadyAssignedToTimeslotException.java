package com.danielbenami.dropit.exception;

public class AddressAlreadyAssignedToTimeslotException extends RuntimeException{

    public AddressAlreadyAssignedToTimeslotException(String message){
        super(message);
    }
}
