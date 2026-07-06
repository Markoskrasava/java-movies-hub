package ru.practicum.moviehub.store;

import ru.practicum.moviehub.model.Movie;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MoviesStore {
    private List<Movie> movies;
    private int nextId;

    public MoviesStore() {
        this.movies = new ArrayList<>();
        this.nextId = 1;
    }

    public List<Movie> getAllMovies() {
        return new ArrayList<>(movies);
    }

    public void addMovie(Movie movie) {
        System.out.println("Добавлен фильм: " + movie.getTitle() + ", год: " + movie.getYear());
        Movie newMovie = new Movie(nextId, movie.getTitle(), movie.getYear());
        movies.add(newMovie);
        nextId++;
    }

    public void deleteAllMovies() {
        System.out.println("Удалены все фильмы");
        movies.clear();
    }

    public boolean deleteMovie(int id) {
        Iterator<Movie> iterator = movies.iterator();
        while (iterator.hasNext()) {
            Movie movie = iterator.next();
            if (movie.getId() == id) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    public Optional<Movie> findMovie(String title, int year) {
        for (Movie movie : movies) {
            if (movie.getTitle().equals(title) && movie.getYear() == year) {
                return Optional.of(movie);
            }
        }
        return Optional.empty();
    }

    public void clear() {
        movies.clear();
    }

}