package ru.practicum.moviehub.http;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import com.google.gson.Gson;
import ru.practicum.moviehub.api.NotSupportedErrorResponse;
import ru.practicum.moviehub.api.ValidationErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.PostedMovie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static ru.practicum.moviehub.api.ErrorDetails.*;

public class MoviesHandler extends BaseHttpHandler {
    //для эндпойнтов GET /movies, GET /movies?year=YYYY и POST /movies
    private static final int QUERY_LENGTH = 9;
    private static final int FILTER_LENGTH = 5;
    private MoviesStore moviesStore = new MoviesStore();
    private Gson gson = new Gson();

    @Override
    public void handle(HttpExchange ex) throws IOException {
        try {
            String method = ex.getRequestMethod();

            if (method.equalsIgnoreCase("GET")) {
                String query = ex.getRequestURI().getRawQuery();

                if (query != null) {
                    if (query.startsWith("year=") && query.length() == QUERY_LENGTH) {
                        String reqYear = query.substring(FILTER_LENGTH);

                        if (moviesStore.isInteger(reqYear)) {
                            int year = Integer.parseInt(reqYear);

                            sendJson(ex, 200, gson.toJson(moviesStore.filterMovies(year)));
                        } else {
                            //если год из запроса не парсится в строку
                            throw new ValidationErrorResponse(new String[]{INVALID_YEAR_FORMAT});
                        }
                    } else {
                        //если параметры строки запроса некорректны
                        throw new ValidationErrorResponse(new String[]{INVALID_PARAMETERS});
                    }
                } else {
                    sendJson(ex, 200, gson.toJson(MoviesStore.getAllMovies()));
                }
            } else if (method.equalsIgnoreCase("POST")) {
                if (!isRequestHeaderCorrect(ex)) {
                    throw new NotSupportedErrorResponse(new String[]{INVALID_REQUEST_HEADER});
                }

                MoviesStore moviesStore = new MoviesStore();
                Movie movie = convertToMovie(ex);

                sendJson(ex, 201, gson.toJson(moviesStore.postMovie(movie)));
            } else {
                throw new NotSupportedErrorResponse(new String[]{METHOD_NOT_SUPPORTED});
            }
        } catch (ValidationErrorResponse e) {
            if (e.getMessage().contains(INVALID_YEAR_FORMAT)) {
                sendError(ex, 400, e.getMessage());
            } else {
                sendError(ex, 422, e.getMessage());
            }
        } catch (NotSupportedErrorResponse e) {
            if (e.getMessage().contains(INVALID_REQUEST_HEADER)) {
                sendError(ex, 415, e.getMessage());
            } else {
                sendError(ex, 405, e.getMessage());
            }
        } catch (Exception e) {
            sendError(ex, 500, INTERNAL_ERROR);
        }
    }

    private Movie convertToMovie(HttpExchange ex) throws IOException, ValidationErrorResponse {
        try {
            InputStream inputStream = ex.getRequestBody();
            String jsonMovie = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

            return gson.fromJson(jsonMovie, Movie.class);
        } catch (JsonParseException | ClassCastException e) {
            throw new ValidationErrorResponse(new String[]{INVALID_JSON_OBJECT});
        }
    }
}