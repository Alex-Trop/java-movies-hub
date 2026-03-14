package ru.practicum.moviehub.model;

import ru.practicum.moviehub.store.MoviesStore;

public class PostedMovie {
    //этот объект будет возвращать сервер
    private final int id;
    private final Movie movie;
    private static final MoviesStore STORE = new MoviesStore();

    public PostedMovie(Movie movie) {
        this.id = STORE.getId(movie);
        this.movie = movie;
    }

    @Override
    public String toString() {
        return "id: " + id + movie;
    }
}
