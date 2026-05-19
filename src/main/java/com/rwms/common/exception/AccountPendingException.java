package com.rwms.common.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class AccountPendingException extends RuntimeException {
    public AccountPendingException() {
        super("Account pending manager approval");
    }
}
