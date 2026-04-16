package com.example.ingestion.adapter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.example.common.model.CommonEvent;

@Component
public class RfidAdapter implements DeviceEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(RfidAdapter.class);

    @Override
    public boolean supports(String vendorType) {
        return vendorType != null && "RFID".equalsIgnoreCase(vendorType);
    }

    @Override
    public CommonEvent adapt(Map<String, Object> payload) {

        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("RFID payload cannot be null or empty");
        }

        // ✅ Required fields (fail fast)
        String tagId = requireString(payload, "tagId");
        String zone = requireString(payload, "zone");

        // Optional fields
        String epc = getString(payload, "epc");
        String readerId = getString(payload, "readerId");
        String eventType = getString(payload, "eventType");
        Integer antennaPort = getInteger(payload, "antennaPort");
        Integer readCount = getInteger(payload, "readCount");
        Double rssi = getDouble(payload, "rssi");

        LocalDateTime eventTimestamp = parseTimestamp(payload.get("timestamp"));

        // Nested location
        Map<String, Object> locationMap = getMap(payload, "location");
        String storeId = getString(locationMap, "storeId");
        String aisle = getString(locationMap, "aisle");

        log.info("Adapting RFID payload for tagId={}, zone={}", tagId, zone);

        // ✅ Canonical payload
        Map<String, Object> canonicalPayload = new HashMap<>();
        canonicalPayload.put("tagId", tagId);
        canonicalPayload.put("epc", epc);
        canonicalPayload.put("readerId", readerId);
        canonicalPayload.put("antennaPort", antennaPort);
        canonicalPayload.put("zone", zone);
        canonicalPayload.put("eventType", eventType != null ? eventType : "SCAN");
        canonicalPayload.put("rssi", rssi);
        canonicalPayload.put("readCount", readCount);
        canonicalPayload.put("storeId", storeId);
        canonicalPayload.put("aisle", aisle);

        // ✅ Schema versioning (important for future Kafka evolution)
        canonicalPayload.put("schemaVersion", "v1");
        
        Object simulateFailure = payload.get("simulateFailure");
        canonicalPayload.put("simulateFailure", simulateFailure);

        // Optional: trace/debug (can remove later if payload is huge)
        // canonicalPayload.put("rawEvent", payload);

        // ✅ Build CommonEvent
        CommonEvent event = new CommonEvent();
        event.setEventId(UUID.randomUUID().toString());

        // Standard rule: deviceId = primary identifier
        event.setDeviceId(tagId);

        event.setEventType(eventType != null ? eventType : "RFID_SCAN");
        event.setTimestamp(eventTimestamp);
        event.setPayload(canonicalPayload);

        return event;
    }

    // =========================
    // Helper Methods
    // =========================

    private String requireString(Map<String, Object> map, String key) {
        String value = getString(map, key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required field: " + key);
        }
        return value;
    }

    private String getString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }

        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.intValue();
        }

        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer value for key: " + key);
        }
    }

    private Double getDouble(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }

        Object value = map.get(key);
        if (value instanceof Number number) {
            return number.doubleValue();
        }

        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid double value for key: " + key);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return Map.of();
        }

        Object value = map.get(key);
        if (value instanceof Map<?, ?> nestedMap) {
            return (Map<String, Object>) nestedMap;
        }

        throw new IllegalArgumentException("Invalid nested map for key: " + key);
    }

    private LocalDateTime parseTimestamp(Object timestampValue) {

        if (timestampValue == null) {
            log.warn("Missing timestamp, using system time");
            return LocalDateTime.now();
        }

        String timestamp = String.valueOf(timestampValue);

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