package com.example.ingestion.publisher;

import com.example.common.model.CommonEvent;

public interface EventPublisher {
    void publish(CommonEvent event);
}