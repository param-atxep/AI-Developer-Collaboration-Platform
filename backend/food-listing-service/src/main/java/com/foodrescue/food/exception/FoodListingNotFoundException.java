package com.foodrescue.food.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FoodListingNotFoundException extends RuntimeException {

    public FoodListingNotFoundException(String message) {
        super(message);
    }

    public FoodListingNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
