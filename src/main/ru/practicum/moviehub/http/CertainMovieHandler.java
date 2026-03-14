package ru.practicum.moviehub.http;

import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import ru.practicum.moviehub.api.NotFoundErrorResponse;
import ru.practicum.moviehub.api.NotSupportedErrorResponse;
import ru.practicum.moviehub.api.ValidationErrorResponse;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;

import static ru.practicum.moviehub.api.ErrorDetails.INVALID_ID;
import static ru.practicum.moviehub.api.ErrorDetails.METHOD_NOT_SUPPORTED;

public class CertainMovieHandler extends BaseHttpHandler {
    //для эндпойнтов GET /movies/{id} и DELETE /movies/{id}
    Gson gson = new Gson();
    MoviesStore moviesStore = new MoviesStore();


    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();

            if (!moviesStore.isInteger(ex.getRequestURI().getPath().split("/")[2])) {
                throw new ValidationErrorResponse(new String[] {INVALID_ID});
            }

            int id = Integer.parseInt(ex.getRequestURI().getPath().split("/")[2]);

            if (method.equalsIgnoreCase("DELETE")) {
                moviesStore.delete(id);
                sendNoContent(ex);
            } else if (method.equalsIgnoreCase("GET")) {
                sendJson(ex, 200, gson.toJson(moviesStore.getMovie(id)));
            } else {
                throw new NotSupportedErrorResponse(new String[] {METHOD_NOT_SUPPORTED});
            }
        } catch (NotFoundErrorResponse e) {
            sendJson(ex, 404, gson.toJson(e.getMessage()));
        } catch (ValidationErrorResponse e) {
            sendJson(ex, 400, gson.toJson(e.getMessage()));
        } catch (NotSupportedErrorResponse e) {
            sendJson(ex, 405, gson.toJson(e.getMessage()));
        }
    }

}
