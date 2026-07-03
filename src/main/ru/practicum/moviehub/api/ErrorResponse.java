package ru.practicum.moviehub.api;

public class ErrorResponse {
    private String error;
    private final int statusCode;


    public ErrorResponse(String error, int statusCode) {
        this.error = error;
        this.statusCode = statusCode;
    }


    public String getError() {
        return error;
    }

    public int getStatusCode() {
        return statusCode;
    }
}