package org.example.librarymanagement.dto;

import java.time.LocalDateTime;

public class ErrorResponse {

    private Integer status;
    private String message;
    private LocalDateTime timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(
            Integer status,
            String message,
            LocalDateTime timestamp
    ) {
        this.status = status;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
