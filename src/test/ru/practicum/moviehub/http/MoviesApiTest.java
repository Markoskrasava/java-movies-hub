package ru.practicum.moviehub.http;

import java.util.List;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import com.google.gson.Gson;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.practicum.moviehub.model.Movie;

import static org.junit.jupiter.api.Assertions.*;

public class MoviesApiTest {
    public static final String BASE = "http://localhost:8080"; // !!! добавьте базовую часть URL
    private static MoviesServer server;
    private static HttpClient client;
    private static final Gson gson = new Gson();

    @BeforeAll
    static void beforeAll() {
        // !!! Реализуйте метод beforeAll
        server = new MoviesServer();
        server.start();
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    @BeforeEach
    void beforeEach() {
        server.getStore().clear();
    }

    @AfterAll
    static void afterAll() {
        // !!! Реализуйте метод afterAll
        if (server != null) {
            server.stop();
        }
    }

    @Test
    void getMovies_whenEmpty_returnsEmptyArray() throws Exception {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies")) // !!! Добавьте правильный URI
                .GET()
                .build();

        HttpResponse<String> resp =
                client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode(), "GET /movies должен вернуть 200");

        String contentTypeHeaderValue =
                resp.headers().firstValue("Content-Type").orElse("");
        assertEquals("application/json; charset=UTF-8", contentTypeHeaderValue,
                "Content-Type должен содержать формат данных и кодировку");

        String body = resp.body().trim();
        assertTrue(body.startsWith("[") && body.endsWith("]"),
                "Ожидается JSON-массив");
    }

    @Test
    void getMovies_returnAllMovies() throws Exception {
        Movie movie1 = new Movie("Начало", 2010, "Кристофер Нолан");
        Movie movie2 = new Movie("Матрица", 1999, "Вачовски");
        Movie movie3 = new Movie("Криминальное чтиво", 1994, "Квентин Тарантино");
        String json1 = gson.toJson(movie1);
        HttpRequest postReq1 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json1))
                .build();
        client.send(postReq1, HttpResponse.BodyHandlers.discarding());

        String json2 = gson.toJson(movie2);
        HttpRequest postReq2 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json2))
                .build();
        client.send(postReq2, HttpResponse.BodyHandlers.discarding());

        String json3 = gson.toJson(movie3);
        HttpRequest postReq3 = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json3))
                .build();
        client.send(postReq3, HttpResponse.BodyHandlers.discarding());

        HttpRequest getReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> resp = client.send(getReq,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(200, resp.statusCode());

        List<Movie> movies = gson.fromJson(resp.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(3, movies.size());
    }

    @Test
    void postMovies_shouldAddMovie() throws Exception {
        Movie newMovie = new Movie("Бойцовский клуб", 1999, "Дэвид Финчер");
        String json = gson.toJson(newMovie);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> resp = client.send(req,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, resp.statusCode());

        Movie saved = gson.fromJson(resp.body(), Movie.class);
        assertNotNull(saved);
        assertEquals("Бойцовский клуб", saved.getName());
        assertEquals(1999, saved.getYear());
        assertEquals("Дэвид Финчер", saved.getDirector());
    }

    @Test
    void deleteMovies_shouldDeleteAll() throws Exception {
        Movie newMovie = new Movie("Бойцовский клуб", 1999, "Дэвид Финчер");
        String json = gson.toJson(newMovie);

        HttpRequest postReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> postResp = client.send(postReq,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        assertEquals(201, postResp.statusCode());

        HttpRequest getReqBefore = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> getRespBefore = client.send(getReqBefore,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        List<Movie> moviesBefore = gson.fromJson(getRespBefore.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(1, moviesBefore.size());

        HttpRequest deleteReq = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .DELETE()
                .build();

        HttpResponse<Void> deleteResp = client.send(deleteReq,
                HttpResponse.BodyHandlers.discarding());

        assertEquals(200, deleteResp.statusCode());

        HttpRequest getReqAfter = HttpRequest.newBuilder()
                .uri(URI.create(BASE + "/movies"))
                .GET()
                .build();

        HttpResponse<String> getRespAfter = client.send(getReqAfter,
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        List<Movie> moviesAfter = gson.fromJson(getRespAfter.body(), new ListOfMoviesTypeToken().getType());
        assertEquals(0, moviesAfter.size());
        assertEquals("[]", getRespAfter.body().trim());
    }
}