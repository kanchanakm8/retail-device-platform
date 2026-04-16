package com.example.ingestion.publisher;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;

import com.example.common.model.CommonEvent;
import com.example.ingestion.entity.DlqEventEntity;
import com.example.ingestion.repository.DlqEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(RestEventPublisher.class);

    // Circuit breaker fields
    private int failureCount = 0;
    private final int failureThreshold = 3;
    private long circuitOpenUntil = 0;
    private final long openDurationMillis = 30000;

    // Retry fields
    private final int maxRetries = 3;
    private final long retryBackoffMillis = 1000;

    private final RestTemplate restTemplate;
    private final DlqEventRepository dlqRepository;
    private final String inventoryServiceProcessUrl;
    private final ObjectMapper objectMapper;

    public RestEventPublisher(RestTemplate restTemplate,
                              DlqEventRepository dlqRepository,
                              String inventoryServiceProcessUrl,
                              ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.dlqRepository = dlqRepository;
        this.inventoryServiceProcessUrl = inventoryServiceProcessUrl;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(CommonEvent event) {
        log.info("Publisher payload = {}", event.getPayload());

        if (isCircuitOpen()) {
            log.warn("Circuit is OPEN. Skipping publish for eventId={}", event.getEventId());
            sendToDeadLetterQueue(event, "Circuit is open. Downstream service temporarily unavailable");
            return;
        }

        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                attempt++;
                log.info("Attempt {}: Publishing eventId={} to inventory-service", attempt, event.getEventId());

                Object simulateFailure = event.getPayload() != null
                        ? event.getPayload().get("simulateFailure")
                        : null;

                if (Boolean.TRUE.equals(simulateFailure)) {
                    log.warn("Simulating publish failure for eventId={}", event.getEventId());
                    throw new RuntimeException("Simulated downstream failure for testing");
                }

                restTemplate.postForObject(inventoryServiceProcessUrl, event, String.class);

                log.info("Successfully published eventId={}", event.getEventId());

                resetCircuitBreaker();
                return;

            } catch (Exception e) {
                lastException = e;
                log.warn("Attempt {} failed for eventId={}", attempt, event.getEventId(), e);

                try {
                    Thread.sleep(retryBackoffMillis);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.error("Retry sleep interrupted for eventId={}", event.getEventId(), ie);
                    break;
                }
            }
        }

        onPublishFailure(event, lastException);
    }

    private boolean isCircuitOpen() {
        return System.currentTimeMillis() < circuitOpenUntil;
    }

    private void resetCircuitBreaker() {
        failureCount = 0;
        circuitOpenUntil = 0;
    }

    private void onPublishFailure(CommonEvent event, Exception lastException) {
        failureCount++;

        log.error("All retries failed for eventId={}. failureCount={}", event.getEventId(), failureCount);

        if (failureCount >= failureThreshold) {
            circuitOpenUntil = System.currentTimeMillis() + openDurationMillis;
            log.error("Circuit opened for {} ms due to repeated failures", openDurationMillis);
        }

        String reason = "REST call failed after retries";
        if (lastException != null && lastException.getMessage() != null) {
            reason = lastException.getMessage();
        }

        sendToDeadLetterQueue(event, reason);
    }

    private void sendToDeadLetterQueue(CommonEvent event, String reason) {
        DlqEventEntity dlq = new DlqEventEntity();
        dlq.setEventId(event.getEventId());
        dlq.setPayloadJson(convertObjectToJson(event));
        dlq.setReason(reason);
        dlq.setFailedAt(LocalDateTime.now());

        dlqRepository.save(dlq);

        log.error("Event moved to DLQ: eventId={}, reason={}", event.getEventId(), reason);
    }

    private String convertObjectToJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert object to JSON", e);
        }
    }
}