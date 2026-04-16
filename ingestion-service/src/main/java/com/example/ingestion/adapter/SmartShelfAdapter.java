package com.example.ingestion.adapter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.example.common.model.CommonEvent;

@Component
public class SmartShelfAdapter implements DeviceEventAdapter {

    @Override
    public boolean supports(String vendorType) {
        return vendorType != null && "SMART_SHELF".equalsIgnoreCase(vendorType);
    }

    @Override
    public CommonEvent adapt(Map<String, Object> payload) {

        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("SmartShelf payload cannot be null or empty");
        }

        String shelfId = getString(payload, "shelfId");
        String storeId = getString(payload, "storeId");
        String productId = getString(payload, "productId");
        Integer itemCount = getInteger(payload, "itemCount");
        Integer capacity = getInteger(payload, "capacity");
        String eventType = getString(payload, "eventType");
        Double weight = getDouble(payload, "weight");

        LocalDateTime timestamp = parseTimestamp(payload.get("timestamp"));

        if (shelfId == null || shelfId.isBlank()) {
            throw new IllegalArgumentException("shelfId is required");
        }

        if (itemCount == null) {
            throw new IllegalArgumentException("itemCount is required");
        }

        // 🔥 Business Logic (important for interview)
        String derivedEventType = deriveEventType(itemCount, capacity, eventType);

        Map<String, Object> canonicalPayload = new HashMap<>();
        canonicalPayload.put("shelfId", shelfId);
        canonicalPayload.put("storeId", storeId);
        canonicalPayload.put("productId", productId);
        canonicalPayload.put("itemCount", itemCount);
        canonicalPayload.put("capacity", capacity);
        canonicalPayload.put("weight", weight);
        canonicalPayload.put("eventType", derivedEventType);

        CommonEvent event = new CommonEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setDeviceId(shelfId);
        event.setEventType("SMART_SHELF_EVENT");
        event.setTimestamp(timestamp);
        event.setPayload(canonicalPayload);

        return event;
    }

    // ---------------- Helper Methods ----------------

    private String deriveEventType(Integer itemCount, Integer capacity, String eventType) {

        if (eventType != null && !eventType.isBlank()) {
            return eventType; // prefer vendor event if given
        }

        if (itemCount == 0) {
            return "OUT_OF_STOCK";
        }

        if (capacity != null && itemCount < (capacity * 0.2)) {
            return "LOW_STOCK";
        }

        return "STOCK_UPDATE";
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return null;
        return String.valueOf(map.get(key));
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return null;

        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer for key: " + key);
        }
    }

    private Double getDouble(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) return null;

        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double for key: " + key);
        }
    }

    private LocalDateTime parseTimestamp(Object value) {
        if (value == null) return LocalDateTime.now();

        String timestamp = String.valueOf(value);

        try {
            return OffsetDateTime.parse(timestamp).toLocalDateTime();
        } catch (DateTimeParseException e) {
            try {
                return LocalDateTime.parse(timestamp);
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Invalid timestamp format: " + timestamp);
            }
        }
    }
}