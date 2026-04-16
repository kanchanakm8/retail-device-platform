package com.example.ingestion.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.common.model.CommonEvent;

public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    @Override
    public void publish(CommonEvent event) {
        log.info("Publishing eventId={} to Kafka (simulation)", event.getEventId());

        // future:
        // kafkaTemplate.send(topic, event.getEventId(), event);
    }
}