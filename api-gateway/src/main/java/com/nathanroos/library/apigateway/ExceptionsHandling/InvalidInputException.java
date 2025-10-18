package com.nathanroos.library.apigateway.ExceptionsHandling;

public class InvalidInputException extends RuntimeException {
    public InvalidInputException(String message) {
        super(message);
    }
}