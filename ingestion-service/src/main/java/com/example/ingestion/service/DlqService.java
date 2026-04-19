package com.example.ingestion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.common.model.CommonEvent;
import com.example.ingestion.dto.DlqEventResponse;
import com.example.ingestion.entity.DlqEventEntity;
import com.example.ingestion.entity.DlqStatus;
import com.example.ingestion.publisher.EventPublisher;
import com.example.ingestion.publisher.RestEventPublisher;
import com.example.ingestion.repository.DlqEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DlqService {

    private final DlqEventRepository dlqEventRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public DlqService(DlqEventRepository dlqEventRepository,
                      EventPublisher eventPublisher,
                      ObjectMapper objectMapper) {
        this.dlqEventRepository = dlqEventRepository;
        this.eventPublisher = eventPublisher;
        this.objectMapper = objectMapper;
    }

    public List<DlqEventResponse> getAllDlqEvents() {
        return dlqEventRepository.findAll().stream()
                .map(dlq -> new DlqEventResponse(
                        dlq.getEventId(),
                        dlq.getPayloadJson(),
                        dlq.getReason(),
                        dlq.getFailedAt()))
                .collect(Collectors.toList());
    }

    public DlqEventResponse getDlqEventById(String eventId) {
        DlqEventEntity dlq = dlqEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("DLQ event not found: " + eventId));

        return new DlqEventResponse(
                dlq.getEventId(),
                dlq.getPayloadJson(),
                dlq.getReason(),
                dlq.getFailedAt());
    }

    public void reprocess(String eventId) {
        DlqEventEntity dlq = dlqEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("DLQ event not found: " + eventId));

        try {
            dlq.setStatus(DlqStatus.REPROCESSING);
            dlq.setLastRetriedAt(LocalDateTime.now());
            dlq.setRetryCount(dlq.getRetryCount() == null ? 1 : dlq.getRetryCount() + 1);
            dlqEventRepository.save(dlq);

            CommonEvent event = objectMapper.readValue(dlq.getPayloadJson(), CommonEvent.class);

            Map<String, Object> payload = event.getPayload();
            if (payload != null && payload.containsKey("simulateFailure")) {
                payload.remove("simulateFailure");
            }

            if (eventPublisher instanceof RestEventPublisher restEventPublisher) {
                restEventPublisher.publishFromReprocess(event);
            } else {
                eventPublisher.publish(event);
            }

            dlq.setStatus(DlqStatus.REPROCESSED_SUCCESS);
            dlq.setResolvedAt(LocalDateTime.now());
            dlq.setLastError(null);
            dlqEventRepository.save(dlq);

        } catch (Exception e) {
            dlq.setStatus(DlqStatus.REPROCESSED_FAILED);
            dlq.setLastError(e.getMessage());
            dlq.setLastRetriedAt(LocalDateTime.now());
            dlqEventRepository.save(dlq);

            throw new RuntimeException("Failed to reprocess DLQ event: " + eventId, e);
        }
    }
}