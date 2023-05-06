package com.danielbenami.dropit.exception;

public class TimeslotNotFoundException extends RuntimeException{

    public TimeslotNotFoundException(String message){
        super(message);
    }
}
