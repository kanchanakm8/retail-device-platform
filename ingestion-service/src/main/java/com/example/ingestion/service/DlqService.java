package com.example.ingestion.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.common.model.CommonEvent;
import com.example.ingestion.dto.DlqEventResponse;
import com.example.ingestion.entity.DlqEventEntity;
import com.example.ingestion.publisher.EventPublisher;
import com.example.ingestion.repository.DlqEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DlqService {

    private static final Logger log = LoggerFactory.getLogger(DlqService.class);

    private final DlqEventRepository dlqEventRepository;
    private final EventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public DlqService(DlqEventRepository dlqEventRepository, EventPublisher eventPublisher, ObjectMapper objectMapper) {
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

        return new DlqEventResponse(dlq.getEventId(), dlq.getPayloadJson(), dlq.getReason(), dlq.getFailedAt());
    }

    public void reprocess(String eventId) {
        DlqEventEntity dlq = dlqEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("DLQ event not found: " + eventId));

        try {
            CommonEvent event = objectMapper.readValue(dlq.getPayloadJson(), CommonEvent.class);

            Map<String, Object> payload = event.getPayload();
            if (payload != null && payload.containsKey("simulateFailure")) {
                payload.remove("simulateFailure");
            }

            log.info("Reprocessing DLQ eventId={}", eventId);
            eventPublisher.publish(event);

            dlqEventRepository.deleteById(eventId);
            log.info("Successfully reprocessed and removed DLQ eventId={}", eventId);

        } catch (Exception e) {
            throw new RuntimeException("Failed to reprocess DLQ event: " + eventId, e);
        }
    }
}