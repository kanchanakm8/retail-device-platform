package com.example.ingestion.dto;

import java.time.LocalDateTime;

public class DlqEventResponse {

    private String eventId;
    private String payloadJson;
    private String reason;
    private LocalDateTime failedAt;

    public DlqEventResponse() {
    }

    public DlqEventResponse(String eventId, String payloadJson, String reason, LocalDateTime failedAt) {
        this.eventId = eventId;
        this.payloadJson = payloadJson;
        this.reason = reason;
        this.failedAt = failedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getPayloadJson() {
        return payloadJson;
    }

    public void setPayloadJson(String payloadJson) {
        this.payloadJson = payloadJson;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public LocalDateTime getFailedAt() {
        return failedAt;
    }

    public void setFailedAt(LocalDateTime failedAt) {
        this.failedAt = failedAt;
    }
}