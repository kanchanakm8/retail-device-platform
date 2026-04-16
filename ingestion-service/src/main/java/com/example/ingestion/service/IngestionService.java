package com.example.ingestion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.common.model.CommonEvent;
import com.example.ingestion.adapter.DeviceEventAdapter;
import com.example.ingestion.dto.DeviceEventRequest;
import com.example.ingestion.dto.EventResponse;
import com.example.ingestion.factory.AdapterFactory;
import com.example.ingestion.publisher.EventPublisher;

@Service
public class IngestionService {

    private final AdapterFactory adapterFactory;
    private final EventPublisher eventPublisher;

    public IngestionService(AdapterFactory adapterFactory, EventPublisher eventPublisher) {
        this.adapterFactory = adapterFactory;
        this.eventPublisher = eventPublisher;
        System.out.println("IngestionService initialized with publisher: " + eventPublisher);
    }

    public EventResponse ingest(DeviceEventRequest request) {
        DeviceEventAdapter adapter = adapterFactory.getAdapter(request.getVendorType());
        CommonEvent commonEvent = adapter.adapt(request.getPayload());

        eventPublisher.publish(commonEvent);

        return new EventResponse("Event accepted successfully", commonEvent.getEventId(), "ACCEPTED");
    }
}