package ru.practicum.moviehub.http;

import com.google.gson.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.api.NotFoundErrorResponse;
import ru.practicum.moviehub.api.NotSupportedErrorResponse;
import ru.practicum.moviehub.api.ValidationErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.practicum.moviehub.api.ErrorDetails.*;

public class MoviesApiTest {
    private static final String BASE = "http://localhost:8080";
    private static final String CT_JSON = "application/json; charset=UTF-8";
    private static MoviesServer server;
    private static HttpClient client;
    private static MoviesStore store = new MoviesStore();
    private static Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        server = new MoviesServer(new MoviesStore(), MoviesServer.PORT);

        server.start();

        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        store.clear();
    }

    @AfterAll
    static void afterAll() {
        server.stop();
    }

    @Test
    void returns_NotSupportedErrorResponse_when_method_is_not_supported() throws IOException, InterruptedException {
        Movie movie = new Movie("Фильм1", 2000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(405, resp.statusCode(), "PUT /movies должен вернуть 405");

        String body = JsonParser.parseString(resp.body()).getAsString();
        String expected = NotSupportedErrorResponse.ERROR + ": " + Arrays.toString(new String[]{METHOD_NOT_SUPPORTED});

        assertEquals(expected, body, "Должен возвращать NotSupportedErrorResponse");
    }

    //На GET/movies
    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject body = JsonParser.parseString(resp.body()).getAsJsonObject();
        JsonObject expected = new JsonObject();

        assertTrue(body.equals(expected), "Ожидается пустой JSON-объект");
    }

    @Test
    void getMovies_returns_postedMovies() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String movie1Id = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsString();

        Movie movie2 = new Movie("Фильм2", 2010);

        HttpRequest reqPostMovie2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie2)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie2 =
                client.send(reqPostMovie2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String movie2Id = JsonParser.parseString(respMovie2.body()).getAsJsonObject().get("id").getAsString();

        HttpRequest getAllMoviesReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getAllMoviesResp =
                client.send(getAllMoviesReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getAllMoviesResp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                getAllMoviesResp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject postedMovie1 = JsonParser.parseString(getAllMoviesResp.body()).getAsJsonObject()
                .get(movie1Id).getAsJsonObject();
        JsonObject postedMovie2 = JsonParser.parseString(getAllMoviesResp.body()).getAsJsonObject()
                .get(movie2Id).getAsJsonObject();

        Movie deserializedMovie1 = gson.fromJson(postedMovie1, Movie.class);
        Movie deserializedMovie2 = gson.fromJson(postedMovie2, Movie.class);

        assertTrue(movie1.equals(deserializedMovie1) && movie2.equals(deserializedMovie2));
    }

    //На POST/movies
    @Test
    void posts_valid_movie() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String movie1Id = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsString();

        assertEquals(201, respMovie1.statusCode(), "POST /movies должен вернуть 201");

        String contentTypeHeaderValue =
                respMovie1.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject postedMovie1 = JsonParser.parseString(respMovie1.body()).getAsJsonObject()
                .get("movie").getAsJsonObject();

        Movie deserializedMovie1 = gson.fromJson(postedMovie1, Movie.class);

        assertTrue(movie1.equals(deserializedMovie1));
    }

    @Test
    void returns_ValidationErrorResponse_when_title_is_empty() throws IOException, InterruptedException {
        Movie movie1 = new Movie("", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, respMovie1.statusCode(), "POST /movies должен вернуть 422");

        String body = JsonParser.parseString(respMovie1.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{EMPTY_TITLE});

        assertEquals(expected, body, "Должен возвращать ValidationErrorResponse");
    }

    @Test
    void returns_ValidationErrorResponse_when_title_is_long() throws IOException, InterruptedException {
        Movie movie1 = new Movie("хххххххххххххххххххххххххххххххххххххххххххххххххххххххх" +
                "хххххххххххххххххххххххххххххххххххххххххххххххххх", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, respMovie1.statusCode(), "POST /movies должен вернуть 422");

        String body = JsonParser.parseString(respMovie1.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{LONG_TITLE});

        assertEquals(expected, body, "Должен возвращать ValidationErrorResponse");
    }

    @Test
    void returns_ValidationErrorResponse_when_year_is_invalid() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 1615);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, respMovie1.statusCode(), "POST /movies должен вернуть 422");

        String body1 = JsonParser.parseString(respMovie1.body()).getAsString();
        String expected1 = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_YEAR});

        assertEquals(expected1, body1, "Должен возвращать ValidationErrorResponse");

        Movie movie2 = new Movie("Фильм1", 3015);

        HttpRequest reqPostMovie2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie2)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie2 =
                client.send(reqPostMovie2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, respMovie2.statusCode(), "POST /movies должен вернуть 422");

        String body2 = JsonParser.parseString(respMovie2.body()).getAsString();
        String expected2 = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_YEAR});

        assertEquals(expected2, body2, "Должен возвращать ValidationErrorResponse");
    }

    @Test
    void returns_NotSupportedErrorResponse_when_invalid_ContentType() throws IOException, InterruptedException {
        Movie movie = new Movie("Фильм1", 2000);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .setHeader("Content-Type", "not json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie)))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(415, resp.statusCode(), "PUT /movies должен вернуть 415");

        String body = JsonParser.parseString(resp.body()).getAsString();
        String expected = NotSupportedErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_REQUEST_HEADER});

        assertEquals(expected, body, "Должен возвращать NotSupportedErrorResponse");
    }

    @Test
    void returns_ValidationErrorResponse_when_invalid_JSON() throws IOException, InterruptedException {
        String notAMovie = "Это не фильм";

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .setHeader("Content-Type", CT_JSON)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(notAMovie)))
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(422, resp.statusCode(), "POST /movies должен вернуть 422");

        String body = JsonParser.parseString(resp.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_JSON_OBJECT});

        assertEquals(expected, body, "Должен возвращать ValidationErrorResponse");
    }

    //на GET/movies/{id}
    @Test
    void returns_movie_by_id() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String movie1Id = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsString();

        HttpRequest getMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + movie1Id))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getMovieByIdResp =
                client.send(getMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getMovieByIdResp.statusCode(), "GET /movies/{id} должен вернуть 200");

        String contentTypeHeaderValue =
                getMovieByIdResp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonObject postedMovie1 = JsonParser.parseString(getMovieByIdResp.body()).getAsJsonObject();

        Movie deserializedMovie1 = gson.fromJson(postedMovie1, Movie.class);

        assertTrue(movie1.equals(deserializedMovie1));
    }

    @Test
    void returns_NotFoundErrorResponse_when_wrong_id() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int trueId = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsInt();

        String falseId = String.valueOf(trueId + 1);

        HttpRequest getMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + falseId))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getMovieByIdResp =
                client.send(getMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, getMovieByIdResp.statusCode(), "GET /movies/{id} должен вернуть 404");

        String actual = JsonParser.parseString(getMovieByIdResp.body()).getAsString();
        String expected = NotFoundErrorResponse.ERROR + ": " + Arrays.toString(new String[]{MOVIE_NOT_FOUND});

        assertEquals(expected, actual, "Должен возвращать NotFoundErrorResponse");
    }

    @Test
    void returns_ValidationErrorResponse_when_id_is_not_a_number() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        String falseId = "123bw45";

        HttpRequest getMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + falseId))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getMovieByIdResp =
                client.send(getMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, getMovieByIdResp.statusCode(), "GET /movies/{id} должен вернуть 400");

        String actual = JsonParser.parseString(getMovieByIdResp.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_ID});

        assertEquals(expected, actual, "Должен возвращать ValidationErrorResponse");

    }

    //на DELETE /movies/id
    @Test
    void returns_nothing_when_deletes() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        String movieId = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsString();

        HttpRequest deleteMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + movieId))
                .DELETE()
                .build();

        HttpResponse<String> deleteMovieByIdResp =
                client.send(deleteMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(204, deleteMovieByIdResp.statusCode(), "DELETE /movies/{id} должен вернуть 204");
    }

    @Test
    void returns_NotFoundErrorResponse_when_wrong_id_to_delete() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int trueId = JsonParser.parseString(respMovie1.body()).getAsJsonObject().get("id").getAsInt();

        String falseId = String.valueOf(trueId + 1);

        HttpRequest deleteMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + falseId))
                .DELETE()
                .build();

        HttpResponse<String> deleteMovieByIdResp =
                client.send(deleteMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(404, deleteMovieByIdResp.statusCode(), "DELETE /movies/{id} должен вернуть 404");

        String actual = JsonParser.parseString(deleteMovieByIdResp.body()).getAsString();
        String expected = NotFoundErrorResponse.ERROR + ": " + Arrays.toString(new String[]{MOVIE_NOT_FOUND});

        assertEquals(expected, actual, "Должен возвращать NotFoundErrorResponse");
    }

    @Test
    void returns_ValidationErrorResponse_when_id_to_delete_is_not_a_number() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        String falseId = "123bw45";

        HttpRequest deleteMovieByIdReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies/" + falseId))
                .DELETE()
                .build();

        HttpResponse<String> deleteMovieByIdResp =
                client.send(deleteMovieByIdReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, deleteMovieByIdResp.statusCode(), "DELETE /movies/{id} должен вернуть 400");

        String actual = JsonParser.parseString(deleteMovieByIdResp.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_ID});

        assertEquals(expected, actual, "Должен возвращать ValidationErrorResponse");
    }

    //на GET/movies?year=yyyy
    @Test
    void returns_filtered_movie_list() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respPostMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Movie movie2 = new Movie("Фильм2", 2001);

        HttpRequest reqPostMovie2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie2)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respPostMovie2 =
                client.send(reqPostMovie2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Movie movie3 = new Movie("Фильм3", 2000);

        HttpRequest reqPostMovie3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie3)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respPostMovie3 =
                client.send(reqPostMovie3, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest getFilteredMoviesReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2000"))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getFilteredMoviesResp =
                client.send(getFilteredMoviesReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getFilteredMoviesResp.statusCode(), "GET /movies?year=yyyy должен вернуть 200");

        String contentTypeHeaderValue =
                getFilteredMoviesResp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonArray filteredList = JsonParser.parseString(getFilteredMoviesResp.body()).getAsJsonArray();
        ArrayList<Movie> deserialisedMovieList = gson.fromJson(filteredList, new ListOfMoviesTypeToken().getType());
        ArrayList<Movie> expectedList = new ArrayList<>(List.of(movie1, movie3));
        String message = "Ожидалось " + expectedList + ", а получили " + filteredList;

        assertTrue(deserialisedMovieList.size() == expectedList.size() && deserialisedMovieList.containsAll(expectedList), message);
    }

    @Test
    void returns_empty_when_year_not_found() throws IOException, InterruptedException {
        Movie movie1 = new Movie("Фильм1", 2000);

        HttpRequest reqPostMovie1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie1)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respPostMovie1 =
                client.send(reqPostMovie1, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        Movie movie2 = new Movie("Фильм2", 2001);

        HttpRequest reqPostMovie2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(movie2)))
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> respPostMovie2 =
                client.send(reqPostMovie2, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        HttpRequest getFilteredMoviesReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2015"))
                .GET()
                .setHeader("Content-Type", CT_JSON)
                .build();

        HttpResponse<String> getFilteredMoviesResp =
                client.send(getFilteredMoviesReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, getFilteredMoviesResp.statusCode(), "GET /movies?year=yyyy должен вернуть 200");

        String contentTypeHeaderValue =
                getFilteredMoviesResp.headers().firstValue("Content-Type").orElse("");

        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        JsonArray filteredList = JsonParser.parseString(getFilteredMoviesResp.body()).getAsJsonArray();
        ArrayList<Movie> deserialisedMovieList = gson.fromJson(filteredList, new ListOfMoviesTypeToken().getType());

        assertTrue(deserialisedMovieList.isEmpty(), "Ожидался пустой JSON-объект");
    }

    @Test
    void returns_ValidationErrorResponse_when_year_to_find_is_not_a_number() throws IOException, InterruptedException {
        HttpRequest getFilteredMoviesReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies?year=2a3x"))
                .GET()
                .build();

        HttpResponse<String> getFilteredMoviesResp =
                client.send(getFilteredMoviesReq, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(400, getFilteredMoviesResp.statusCode(), "GET /movies?year=yyyy должен вернуть 400");

        String actual = JsonParser.parseString(getFilteredMoviesResp.body()).getAsString();
        String expected = ValidationErrorResponse.ERROR + ": " + Arrays.toString(new String[]{INVALID_YEAR_FORMAT});

        assertEquals(expected, actual, "Должен возвращать ValidationErrorResponse");
    }
}