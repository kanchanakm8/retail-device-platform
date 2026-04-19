package com.example.ingestion.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "dlq_events")
public class DlqEventEntity {

    @Id
    private String eventId;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String payloadJson;

    @Column(length = 1000)
    private String reason;

    private LocalDateTime failedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DlqStatus status;

    @Column(nullable = false)
    private Integer retryCount = 0;

    private LocalDateTime lastRetriedAt;

    private LocalDateTime resolvedAt;

    @Column(length = 2000)
    private String lastError;

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

    public DlqStatus getStatus() {
        return status;
    }

    public void setStatus(DlqStatus status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public LocalDateTime getLastRetriedAt() {
        return lastRetriedAt;
    }

    public void setLastRetriedAt(LocalDateTime lastRetriedAt) {
        this.lastRetriedAt = lastRetriedAt;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }
}