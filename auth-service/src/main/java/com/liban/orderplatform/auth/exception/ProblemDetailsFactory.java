package com.liban.orderplatform.auth.exception;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public final class ProblemDetailsFactory {

    public ProblemDetailsFactory() {}

    public static ProblemDetail create(int status, String error, String message, String path,
                                               Map<String, List<String>> errors) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatusCode.valueOf(status),
                message);

        problem.setTitle(error);

        if (path != null) {
            problem.setInstance(URI.create(path));
        }

        problem.setProperty("timestamp", LocalDateTime.now());

        if  (errors != null && !errors.isEmpty()) {
            problem.setProperty("errors", errors);
        }

        return problem;
    }
}
