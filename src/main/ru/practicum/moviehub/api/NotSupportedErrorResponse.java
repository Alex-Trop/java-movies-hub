package ru.practicum.moviehub.api;

import java.util.Arrays;

public class NotSupportedErrorResponse extends ErrorResponse {
    public final static String ERROR = "Операция не поддерживается";

    public NotSupportedErrorResponse(String[] details) {
        super(details);
    }

    @Override
    public String getMessage() {
        return ERROR + ": " + Arrays.toString(details);
    }
}
