package com.hetero.utils;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ApiErrorResponse<T> {
    private int status;
    private String message;
    private String error;

    private String timestamp;
    private T data;

    public int getStatus () {
        return status;
    }

    public void setStatus (int status) {
        this.status = status;
    }

    public String getMessage () {
        return message;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public String getError () {
        return error;
    }

    public void setError (String error) {
        this.error = error;
    }

    public String getTimestamp () {
        return timestamp;
    }

    public void setTimestamp () {
        this.timestamp =  LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));;
    }

    public T getData () {
        return data;
    }

    public void setData (T data) {
        this.data = data;
    }

    public ApiErrorResponse(int status, String message, String error, T data) {
        this.status = status;
        this.message = message;
        this.error = error;
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));;
        this.data = data;
    }
}
