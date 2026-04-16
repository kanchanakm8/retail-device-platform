package com.example.inventory.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.common.model.CommonEvent;
import com.example.inventory.dto.EventViewResponse;
import com.example.inventory.service.InventoryProcessorService;

@RestController
@RequestMapping("/inventory")
@CrossOrigin(origins = "*")
public class InventoryController {

    private final InventoryProcessorService inventoryProcessorService;

    public InventoryController(InventoryProcessorService inventoryProcessorService) {
        this.inventoryProcessorService = inventoryProcessorService;
    }

    @PostMapping("/process")
    public String process(@RequestBody CommonEvent event) {
        inventoryProcessorService.process(event);
        return "Processed";
    }

    @GetMapping("/events")
    public ResponseEntity<List<EventViewResponse>> getAllEvents() {
        return ResponseEntity.ok(inventoryProcessorService.getAllEvents());
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<EventViewResponse> getEventById(@PathVariable("eventId") String eventId) {
        return ResponseEntity.ok(inventoryProcessorService.getEventById(eventId));
    }

    @GetMapping("/health")
    public String health() {
        return "inventory-service is up";
    }
    
    @GetMapping("/metrics")
    public Map<String, Integer> metrics() {
        Map<String, Integer> metrics = new HashMap<>();
        metrics.put("processedEvents", inventoryProcessorService.getProcessedCount());
        metrics.put("failedEvents", inventoryProcessorService.getFailedCount());
        metrics.put("duplicateEvents", inventoryProcessorService.getDuplicateCount());
        return metrics;
    }
    
    @GetMapping("/summary")
    public ResponseEntity<java.util.Map<String, Object>> summary() {
        java.util.Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("processedEvents", inventoryProcessorService.getProcessedCount());
        summary.put("failedEvents", inventoryProcessorService.getFailedCount());
        summary.put("duplicateEvents", inventoryProcessorService.getDuplicateCount());
        summary.put("service", "inventory-service");

        return ResponseEntity.ok(summary);
    }
}