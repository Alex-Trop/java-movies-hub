package ru.practicum.moviehub.api;

import java.util.Arrays;

public class NotFoundErrorResponse extends ErrorResponse {
    public static final String ERROR = "В хранилище отсутствует запрашиваемая информация";

    public NotFoundErrorResponse(String[] details) {
        super(details);
    }

    @Override
    public String getMessage() {
        return ERROR + ": " + Arrays.toString(details);
    }
}
