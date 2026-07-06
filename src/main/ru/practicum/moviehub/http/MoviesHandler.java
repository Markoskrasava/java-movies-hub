package ru.practicum.moviehub.http;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import ru.practicum.moviehub.api.ErrorResponse;
import ru.practicum.moviehub.model.Movie;
import ru.practicum.moviehub.store.MoviesStore;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MoviesHandler extends BaseHttpHandler {
    List<String> errors;
    private final MoviesStore store;
    private final Gson gson;

    public MoviesHandler(MoviesStore store) {
        this.store = store;
        this.gson = new Gson();
    }

    @Override
    public void handle(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath();

        if (method.equalsIgnoreCase("GET")) {
            List<Movie> movies = store.getAllMovies();
            String json = gson.toJson(movies);
            sendJson(ex, 200, json);
        } else if (method.equalsIgnoreCase("POST")) {
            InputStreamReader reader = new InputStreamReader(ex.getRequestBody(), StandardCharsets.UTF_8);
            Movie movie;
            try {
                movie = gson.fromJson(reader, Movie.class);
            } catch (JsonSyntaxException e) {
                ErrorResponse error = new ErrorResponse("Ошибка валидации",
                        List.of("Неверный формат JSON"));
                String json = gson.toJson(error);
                sendJson(ex, 422, json);
                return;
            } finally {
                reader.close();
            }

            if (movie == null) {
                ErrorResponse error = new ErrorResponse("Ошибка валидации",
                        List.of("Тело запроса не должно быть пустым"));
                String json = gson.toJson(error);
                sendJson(ex, 422, json);
                return;
            }

            errors = new ArrayList<>();

            String title = movie.getTitle();
            int year = movie.getYear();

            if ((title == null || title.isBlank()) ||
                    (title != null && title.length() > 100) ||
                    (year < 1888 || year > 2026) ||
                    store.findMovie(movie.getTitle(), movie.getYear()).isPresent()) {

                if (title == null || title.isBlank()) {
                    errors.add("название не должно быть пустым");
                }
                if (title != null && title.length() > 100) {
                    errors.add("название не должно превышать 100 символов");
                }
                if (year < 1888 || year > 2026) {
                    errors.add("год должен быть между 1888 и 2026");
                }
                if (store.findMovie(movie.getTitle(), movie.getYear()).isPresent()) {
                    errors.add("фильм с таким названием и годом уже существует");
                }

                ErrorResponse error = new ErrorResponse("Ошибка валидации", errors);
                String json = gson.toJson(error);
                sendJson(ex, 422, json);
                return;
            }

            store.addMovie(movie);
            String json = gson.toJson(movie);
            sendJson(ex, 201, json);
        } else if (method.equalsIgnoreCase("DELETE") && path.startsWith("/movies/")) {
            try {
                String idStr = path.substring(path.lastIndexOf("/") + 1);
                int id = Integer.parseInt(idStr);

                boolean deleted = store.deleteMovie(id);
                if (deleted) {
                    sendNoContent(ex);
                } else {
                    ErrorResponse error = new ErrorResponse("Фильм не найден",
                            List.of("Фильм с id " + id + " не существует"));
                    sendJson(ex, 404, gson.toJson(error));
                }
            } catch (NumberFormatException e) {
                ErrorResponse error = new ErrorResponse("Неверный формат ID",
                        List.of("ID должен быть числом"));
                sendJson(ex, 400, gson.toJson(error));
            }
        } else {
            ErrorResponse error = new ErrorResponse("Метод не найден", errors);
            String json = gson.toJson(error);
            sendJson(ex, 405, json);
        }
    }
}
