package ru.practicum.moviehub.api;

import static ru.practicum.moviehub.model.Movie.MAX_ALLOWED_YEAR;
import static ru.practicum.moviehub.model.Movie.MIN_ALLOWED_YEAR;

public class ErrorDetails {
    public static final String EMPTY_TITLE = "Название не должно быть пустым";
    public static final String LONG_TITLE = "Название не может быть больше 100 символов";
    public static final String INVALID_YEAR = "Год должен быть между " + MIN_ALLOWED_YEAR + " и " + MAX_ALLOWED_YEAR;
    public static final String INVALID_YEAR_FORMAT = "Год поиска указан в некорректном формате";
    public static final String INVALID_ID = "Некорректный ID";
    public static final String METHOD_NOT_SUPPORTED = "Данный метод пока не поддерживается";
    public static final String DUPLICATES_MOVIE = "Данный фильм уже внесен в хранилище";
    public static final String MOVIE_NOT_FOUND = "Фильм не найден";
    public static final String INVALID_PARAMETERS = "Некорректный запрос: переданы неверные параметры строки";
    public static final String INVALID_REQUEST_HEADER = "Запрошен неверный Content-Type";
    public static final String INVALID_JSON_OBJECT = "Переданы некорректные сведения о фильме";
    public static final String INTERNAL_ERROR = "Неизвестная ошибка сервера, попробуйте еще раз";


}
