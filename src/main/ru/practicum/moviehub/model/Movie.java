package ru.practicum.moviehub.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Objects;

import static ru.practicum.moviehub.api.ErrorDetails.*;

public class Movie {
    private String title;
    private int year;
    public static final int MIN_ALLOWED_YEAR = 1888;
    public static final int MAX_ALLOWED_YEAR = LocalDate.now().plusYears(1).getYear();

    public Movie(String title, int year) {
        this.title = title;
        this.year = year;
    }

    public int getYear() {
        return this.year;
    }

    @Override
    public String toString() {
        return title + ", " + year;
    }

    public boolean isTitleShort() {
        boolean isShort = false;

        if (this.title.length() <= 100 ) {
            isShort = true;
        }
        return isShort;
    }

    public boolean isTitleEmpty() {
        boolean isEmpty = false;

        if (this.title.isEmpty()) isEmpty = true;
        return isEmpty;
    }

    public boolean isYearCorrect() {
        boolean isCorrect = false;

        if (this.year >= MIN_ALLOWED_YEAR && this.year <= MAX_ALLOWED_YEAR) {
            isCorrect = true;
        }
        return isCorrect;
    }

    public String[] validateNewMovie() {
        ArrayList<String> details = new ArrayList<>();

        if (!this.isTitleShort()) {
            details.add(LONG_TITLE);
        }
        if (this.isTitleEmpty()) {
            details.add(EMPTY_TITLE);
        }
        if (!this.isYearCorrect()) {
            details.add(INVALID_YEAR);
        }
        return details.toArray(new String[0]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (this.getClass() != o.getClass()) return false;

        Movie m = (Movie) o;

        return Objects.equals(this.title, m.title) && (this.year == m.year);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, year);
    }
}