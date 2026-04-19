package com.example.inventory.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.common.model.CommonEvent;
import com.example.inventory.service.InventoryProcessorService;

@Component
public class InventoryKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaConsumer.class);

    private final InventoryProcessorService inventoryProcessorService;

    public InventoryKafkaConsumer(InventoryProcessorService inventoryProcessorService) {
        this.inventoryProcessorService = inventoryProcessorService;
    }

    //@KafkaListener(topics = "retail-device-events", groupId = "inventory-service-group")
    public void consume(CommonEvent event) {
        log.info("Consumed eventId={} from Kafka", event.getEventId());
        inventoryProcessorService.process(event);
    }
}