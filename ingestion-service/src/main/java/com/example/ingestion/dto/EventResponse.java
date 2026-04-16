package com.example.ingestion.dto;

public class EventResponse {

    private String message;
    private String eventId;
    private String status;

    public EventResponse() {
    }

    public EventResponse(String message, String eventId, String status) {
        this.message = message;
        this.eventId = eventId;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}