package com.example.ingestion.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ingestion.publisher.RestEventPublisher;

@RestController
@CrossOrigin(origins = "*")
public class PublisherMetricsController {

    private final RestEventPublisher restEventPublisher;

    public PublisherMetricsController(RestEventPublisher restEventPublisher) {
        this.restEventPublisher = restEventPublisher;
    }

    @GetMapping("/publisher/metrics")
    public Map<String, Object> getPublisherMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        metrics.put("publishedSuccessCount", restEventPublisher.getPublishedSuccessCount());
        metrics.put("publishedFailureCount", restEventPublisher.getPublishedFailureCount());
        metrics.put("totalDlqRoutedCount", restEventPublisher.getTotalDlqRoutedCount());
        metrics.put("currentDlqBacklogCount", restEventPublisher.getCurrentDlqBacklogCount());
        metrics.put("reprocessSuccessCount", restEventPublisher.getReprocessSuccessCount());
        metrics.put("reprocessFailureCount", restEventPublisher.getReprocessFailureCount());
        return metrics;
    }
}