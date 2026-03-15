package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    protected static final String CT_JSON = "application/json; charset=UTF-8";
    private final Gson gson = new Gson();

    protected void sendJson(HttpExchange ex, int status, String json) throws IOException {
        // общий для всех хендлеров метод
        // для отправки ответа с телом в формате JSON
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, 0);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected void sendNoContent(HttpExchange ex) throws java.io.IOException {
        // общий для всех хендлеров метод
        // для отправки ответа без тела и кодом 204
        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(204, -1);
    }

    protected void sendError(HttpExchange ex, int status, String errorMessage) throws IOException {
        String json = gson.toJson(errorMessage);

        ex.getResponseHeaders().set("Content-Type", CT_JSON);
        ex.sendResponseHeaders(status, 0);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }
    }

    protected boolean isRequestHeaderCorrect(HttpExchange ex) {
        String contentTypeHeaderValue = ex.getRequestHeaders().getFirst("Content-Type");

        if (contentTypeHeaderValue.equals(CT_JSON)) return true;
        return false;
    }
}