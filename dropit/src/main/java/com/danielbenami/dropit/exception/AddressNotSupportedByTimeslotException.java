package com.danielbenami.dropit.exception;

public class AddressNotSupportedByTimeslotException extends RuntimeException{

    public AddressNotSupportedByTimeslotException(String message){
        super(message);
    }
}
