package ru.practicum.moviehub.model;

public class PostedMovie {
    //этот объект будет возвращать сервер
    private final int id;
    private final Movie movie;

    public PostedMovie(Movie movie, int id) {
        this.id = id;
        this.movie = movie;
    }

    @Override
    public String toString() {
        return "id: " + id + movie;
    }
}
