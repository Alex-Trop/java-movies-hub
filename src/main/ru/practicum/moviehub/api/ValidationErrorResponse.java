package ru.practicum.moviehub.api;

import java.util.Arrays;

public class ValidationErrorResponse extends ErrorResponse {
    public static final String ERROR = "Ошибка валидации";

    public ValidationErrorResponse(String[] details) {
        super(details);
    }

    @Override
    public String getMessage() {
        return ERROR + ": " + Arrays.toString(details);
    }
}
