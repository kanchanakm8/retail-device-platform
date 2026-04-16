package com.example.ingestion.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ingestion.dto.DeviceEventRequest;
import com.example.ingestion.dto.EventResponse;
import com.example.ingestion.service.IngestionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/events")
@Validated
@CrossOrigin(origins = "*")
public class IngestionController {

	private final IngestionService ingestionService;

	public IngestionController(IngestionService ingestionService) {
		this.ingestionService = ingestionService;
	}

	@PostMapping
	public ResponseEntity<EventResponse> ingestEvent(@Valid @RequestBody DeviceEventRequest request) {
		EventResponse response = ingestionService.ingest(request);
		return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
	}

	@GetMapping("/health")
	public String health() {
		return "ingestion-service is up";
	}
	
	@GetMapping("/metrics")
	public Map<String, Object> metrics() {
	    return Map.of("status", "UP");
	}

}