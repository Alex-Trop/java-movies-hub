package ru.practicum.moviehub.store;

import ru.practicum.moviehub.api.NotFoundErrorResponse;
import ru.practicum.moviehub.api.ValidationErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.model.PostedMovie;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import static ru.practicum.moviehub.api.ErrorDetails.*;
import static ru.practicum.moviehub.model.Movie.MAX_ALLOWED_YEAR;
import static ru.practicum.moviehub.model.Movie.MIN_ALLOWED_YEAR;

public class MoviesStore {
    private static HashMap<Integer, Movie> moviesStore = new HashMap<>();
    private static final int VOLUME = 1000000;

    public PostedMovie postMovie(Movie movie) throws ValidationErrorResponse {
        if (movie.isTitleShort() && !movie.isTitleEmpty() && movie.isYearCorrect() && !containsMovie(movie)) {
            moviesStore.put(generateId(), movie);

            PostedMovie postedMovie = new PostedMovie(movie, getId(movie));

            return postedMovie;
        } else if (containsMovie(movie)) {
            throw new ValidationErrorResponse(new String[] {DUPLICATES_MOVIE});
        } else {
            throw new ValidationErrorResponse(movie.validateNewMovie());
        }
    }

    public Movie getMovie(int id) throws NotFoundErrorResponse {
        if (!moviesStore.containsKey(id)) {
            throw new NotFoundErrorResponse(new String[]{MOVIE_NOT_FOUND});
        } else {
            return moviesStore.get(id);
        }
    }

    public int getId(Movie movie) {
        for (int id : moviesStore.keySet()) {
            if (moviesStore.get(id).equals(movie)) return id;
        }
        return -1;
    }

    public static HashMap<Integer, Movie> getAllMovies() {
        return moviesStore;
    }

    public void delete(int id) throws NotFoundErrorResponse {
        if (!moviesStore.containsKey(id)) {
            throw new NotFoundErrorResponse(new String[]{MOVIE_NOT_FOUND});
        } else {
            moviesStore.remove(id);
        }
    }

    public void clear() {
        moviesStore.clear();
    }

    public List<Movie> filterMovies(int year) throws ValidationErrorResponse {
        if (year < MIN_ALLOWED_YEAR || year > MAX_ALLOWED_YEAR) {
            throw new ValidationErrorResponse(new String[] {INVALID_YEAR});
        }

        ArrayList<Movie> filteredMovies = new ArrayList<>();

        for (Movie movie : moviesStore.values()) {
            if (movie.getYear() == year) filteredMovies.add(movie);
        }
        return filteredMovies;
    }

    private int generateId() {
        Random random = new Random();
        int id = random.nextInt(VOLUME + 1);

        if (moviesStore.containsKey(id)) {
            generateId();
        }
        return id;
    }

    public boolean isInteger(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) return false;
        }
        return true;
    }

    private boolean containsMovie(Movie movie) {
        for (Movie film : moviesStore.values()) {
            if (film.equals(movie)) {
                return true;
            }
        }
        return false;
    }

}