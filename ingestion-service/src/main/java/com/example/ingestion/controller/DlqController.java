package com.example.ingestion.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ingestion.dto.DlqEventResponse;
import com.example.ingestion.service.DlqService;

@RestController
@RequestMapping("/api/dlq")
public class DlqController {

    private final DlqService dlqService;

    public DlqController(DlqService dlqService) {
        this.dlqService = dlqService;
    }

    @GetMapping
    public ResponseEntity<List<DlqEventResponse>> getAllDlqEvents() {
        return ResponseEntity.ok(dlqService.getAllDlqEvents());
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<DlqEventResponse> getDlqEventById(@PathVariable("eventId") String eventId) {
        return ResponseEntity.ok(dlqService.getDlqEventById(eventId));
    }

    @PostMapping("/{eventId}/reprocess")
    public ResponseEntity<String> reprocess(@PathVariable("eventId") String eventId) {
        dlqService.reprocess(eventId);
        return ResponseEntity.ok("DLQ event reprocessed successfully: " + eventId);
    }
}