package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler { // Расширьте базовый класс BaseHttpHandler
    private final MoviesStore store;
    private final Gson gson;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();

        if (method.equalsIgnoreCase("GET")) {
            // Получаем все фильмы из хранилища
            List<Movie> movies = store.getAllMovies();
            // Преобразуем в JSON
            String json = gson.toJson(movies);
            // Отправляем ответ
            sendJson(ex, 200, json);
        } else if (method.equalsIgnoreCase("POST")) {
            List<Movie> movies = store.getAllMovies();
            InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
            Movie movie;
            movie = gson.fromJson(reader, Movie.class);
            if (!movie.getName().isBlank() && movie.getYear() != 0 && !movie.getDirector().isBlank() && !movies.contains(movie)) {
                store.addMovie(movie);
                String json = gson.toJson(movie);
                sendJson(ex, 201, json);
            } else {
                ErrorResponse error = new ErrorResponse("Неправильный ввод", 400);
                sendJson(ex, 400, gson.toJson(error));
            }
        } else if (method.equalsIgnoreCase("DELETE")) {
            String json = "{\"status\":\"ok\", \"message\":\"All movies deleted\"}";
            sendJson(ex, 200, json);
            store.deleteAllMovies();
        } else {
            ErrorResponse error = new ErrorResponse("Method not allowed", 405);
            String json = gson.toJson(error);
            sendJson(ex, error.getStatusCode(), json);
        }
    }
}
