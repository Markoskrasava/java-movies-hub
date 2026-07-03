package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.List;

public class MoviesStore {
    private List<Movie> movies;


    public MoviesStore() {
        this.movies = new ArrayList<>();
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    public void addMovie(Movie movie) {
        System.out.println("Добавлен фильм: " + movie.getName() + ", год: " + movie.getYear() + ", директор: " + movie.getDirector());
        movies.add(movie);
    }

    public void deleteAllMovies() {
        System.out.println("Удалены все фильмы");
        movies.clear();
    }

    public void clear() {
        movies.clear();
    }

}