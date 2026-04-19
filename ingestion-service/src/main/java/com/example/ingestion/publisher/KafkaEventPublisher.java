package com.example.ingestion.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.kafka.core.KafkaTemplate;

import com.example.common.model.CommonEvent;

public class KafkaEventPublisher implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventPublisher.class);

    //private final KafkaTemplate<String, CommonEvent> kafkaTemplate;
   // private final String topic;

//    public KafkaEventPublisher(KafkaTemplate<String, CommonEvent> kafkaTemplate, String topic) {
//        this.kafkaTemplate = kafkaTemplate;
//        this.topic = topic;
//    }

    @Override
    public void publish(CommonEvent event) {
       // log.info("Publishing eventId={} to Kafka topic={}", event.getEventId(), topic);
     //   kafkaTemplate.send(topic, event.getEventId(), event);
    }
}