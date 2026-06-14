package com.example.project.audit;

import com.example.project.exception.DuplicateUserException;
import org.springframework.http.HttpStatus;

public class ExceptionMapper {
    public static int toStatusCode(Throwable ex) {

        if (ex instanceof IllegalArgumentException) {
            return HttpStatus.BAD_REQUEST.value();
        }

        if (ex instanceof DuplicateUserException) {
            return HttpStatus.CONFLICT.value();
        }

        if (ex instanceof SecurityException) {
            return HttpStatus.FORBIDDEN.value();
        }

        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
