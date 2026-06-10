package com.example.project.audit;

import org.springframework.http.HttpStatus;

public class ExceptionMapper {
    public static int toStatusCode(Throwable ex) {
        // Expand mapping as needed
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }
}
