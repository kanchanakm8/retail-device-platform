package com.example.ingestion.dto;

import java.time.LocalDateTime;

public class EventViewResponse {

    private String eventId;
    private String deviceId;
    private String eventType;
    private LocalDateTime timestamp;
    private String payloadJson;

    public EventViewResponse() {
    }

    public EventViewResponse(String eventId, String deviceId, String eventType, LocalDateTime timestamp, String payloadJson) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payloadJson = payloadJson;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }
}