package com.foodrescue.food.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InvalidFoodListingStateException extends RuntimeException {

    public InvalidFoodListingStateException(String message) {
        super(message);
    }

    public InvalidFoodListingStateException(String message, Throwable cause) {
        super(message, cause);
    }
}
