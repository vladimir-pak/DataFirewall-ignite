package com.gpb.datafirewall.jwt.exception;

public class JwtInvalidSecretException extends RuntimeException {

    public JwtInvalidSecretException() {
        super("Wrong secret");
    }
}