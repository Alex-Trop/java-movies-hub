package ru.practicum.moviehub.api;

public abstract class ErrorResponse extends Exception {
//для ошибок, чтобы ответы сервера были единообразными и удобными для чтения
    String[] details;

    public ErrorResponse(String[] details) {
        this.details = details;
    }

    public abstract String getMessage();
}