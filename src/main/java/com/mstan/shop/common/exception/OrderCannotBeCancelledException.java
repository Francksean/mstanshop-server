package com.mstan.shop.common.exception;

public class OrderCannotBeCancelledException extends RuntimeException {

    public OrderCannotBeCancelledException(String message) {
        super(message);
    }
}
