package com.example.common.model;

import java.time.LocalDateTime;
import java.util.Map;

public class CommonEvent {

    private String eventId;
    private String deviceId;
    private String eventType;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;

    public CommonEvent() {
    }

    public CommonEvent(String eventId, String deviceId, String eventType, LocalDateTime timestamp,
            Map<String, Object> payload) {
        this.eventId = eventId;
        this.deviceId = deviceId;
        this.eventType = eventType;
        this.timestamp = timestamp;
        this.payload = payload;
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

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}