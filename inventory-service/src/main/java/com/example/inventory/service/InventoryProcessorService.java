package com.example.inventory.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.common.model.CommonEvent;
import com.example.inventory.dto.EventViewResponse;
import com.example.inventory.entity.EventEntity;
import com.example.inventory.repository.EventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class InventoryProcessorService {

	private static final Logger log = LoggerFactory.getLogger(InventoryProcessorService.class);

	private final EventRepository eventRepository;
	private final ObjectMapper objectMapper;

	private final AtomicInteger processedCount = new AtomicInteger();
	private final AtomicInteger failedCount = new AtomicInteger();
	private final AtomicInteger duplicateCount = new AtomicInteger();

	public InventoryProcessorService(EventRepository eventRepository, ObjectMapper objectMapper) {
		this.eventRepository = eventRepository;
		this.objectMapper = objectMapper;
	}

	public void process(CommonEvent event) {
		log.info("Processing eventId={}, deviceId={}, eventType={}", event.getEventId(), event.getDeviceId(),
				event.getEventType());

		if (eventRepository.existsById(event.getEventId())) {
			duplicateCount.incrementAndGet();
			log.warn("Duplicate event detected: {}", event.getEventId());
			return;
		}

		EventEntity entity = new EventEntity();
		entity.setEventId(event.getEventId());
		entity.setDeviceId(event.getDeviceId());
		entity.setEventType(event.getEventType());
		entity.setTimestamp(event.getTimestamp());
		entity.setPayloadJson(convertMapToJson(event.getPayload()));

		try {
			eventRepository.save(entity);
			processedCount.incrementAndGet();
		} catch (Exception e) {
			failedCount.incrementAndGet();
			log.warn("Duplicate event detected (DB constraint): {}", event.getEventId());

		}

		log.info("Event saved successfully with eventId={}", event.getEventId());
	}

	public List<EventViewResponse> getAllEvents() {
		return eventRepository
				.findAllByOrderByTimestampDesc().stream().map(event -> new EventViewResponse(event.getEventId(),
						event.getDeviceId(), event.getEventType(), event.getTimestamp(), event.getPayloadJson()))
				.collect(Collectors.toList());
	}

	public EventViewResponse getEventById(String eventId) {
		EventEntity event = eventRepository.findById(eventId)
				.orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));

		return new EventViewResponse(event.getEventId(), event.getDeviceId(), event.getEventType(),
				event.getTimestamp(), event.getPayloadJson());
	}

	private String convertMapToJson(Object payload) {
		try {
			return objectMapper.writeValueAsString(payload);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Failed to convert payload to JSON", e);
		}
	}

	public int getProcessedCount() {
		return processedCount.get();
	}

	public int getFailedCount() {
		return failedCount.get();
	}

	public int getDuplicateCount() {
		return duplicateCount.get();
	}
}