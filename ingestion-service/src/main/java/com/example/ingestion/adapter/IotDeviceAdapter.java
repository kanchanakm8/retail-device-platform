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
public class IotDeviceAdapter implements DeviceEventAdapter {

    private static final Logger log = LoggerFactory.getLogger(IotDeviceAdapter.class);

    @Override
    public boolean supports(String vendorType) {
        return vendorType != null && "IOT_DEVICE".equalsIgnoreCase(vendorType);
    }

    @Override
    public CommonEvent adapt(Map<String, Object> payload) {

        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("IoT device payload cannot be null or empty");
        }

        // ✅ Required fields
        String deviceId = requireString(payload, "deviceId");

        // Optional fields
        String deviceType = getString(payload, "deviceType");
        String deviceModel = getString(payload, "deviceModel");
        String firmwareVersion = getString(payload, "firmwareVersion");
        String connectivityStatus = getString(payload, "connectivityStatus");
        String eventType = getString(payload, "eventType");
        String siteId = getString(payload, "siteId");
        String zone = getString(payload, "zone");
        String ipAddress = getString(payload, "ipAddress");
        String macAddress = getString(payload, "macAddress");

        Integer batteryLevel = getInteger(payload, "batteryLevel");
        Double signalStrength = getDouble(payload, "signalStrength");

        LocalDateTime timestamp = parseTimestamp(payload.get("timestamp"));

        Map<String, Object> metrics = getMap(payload, "metrics");

        log.info("Adapting IoT device payload for deviceId={}", deviceId);

        // ✅ Derive event type
        String derivedEventType = deriveEventType(eventType, connectivityStatus, batteryLevel);

        // ✅ Canonical payload
        Map<String, Object> canonicalPayload = new HashMap<>();
        canonicalPayload.put("deviceId", deviceId);
        canonicalPayload.put("deviceType", deviceType);
        canonicalPayload.put("deviceModel", deviceModel);
        canonicalPayload.put("firmwareVersion", firmwareVersion);
        canonicalPayload.put("connectivityStatus", connectivityStatus);
        canonicalPayload.put("siteId", siteId);
        canonicalPayload.put("zone", zone);
        canonicalPayload.put("ipAddress", ipAddress);
        canonicalPayload.put("macAddress", macAddress);
        canonicalPayload.put("batteryLevel", batteryLevel);
        canonicalPayload.put("signalStrength", signalStrength);
        canonicalPayload.put("metrics", metrics);
        canonicalPayload.put("eventType", derivedEventType);

        // ✅ Schema versioning
        canonicalPayload.put("schemaVersion", "v1");
        
        Object simulateFailure = payload.get("simulateFailure");
        canonicalPayload.put("simulateFailure", simulateFailure);

        // Optional trace/debug
        // canonicalPayload.put("rawEventSize", payload.size());

        // ✅ Build CommonEvent
        CommonEvent event = new CommonEvent();
        event.setEventId(UUID.randomUUID().toString());

        // Standard rule: deviceId = primary identifier
        event.setDeviceId(deviceId);

        event.setEventType(derivedEventType != null ? derivedEventType : "IOT_DEVICE_EVENT");
        event.setTimestamp(timestamp);
        event.setPayload(canonicalPayload);

        return event;
    }

    // =========================
    // Business Logic
    // =========================

    private String deriveEventType(String eventType, String connectivityStatus, Integer batteryLevel) {

        if (eventType != null && !eventType.isBlank()) {
            return eventType;
        }

        if ("OFFLINE".equalsIgnoreCase(connectivityStatus)) {
            return "DEVICE_OFFLINE";
        }

        if (batteryLevel != null && batteryLevel < 20) {
            return "LOW_BATTERY_ALERT";
        }

        return "DEVICE_HEARTBEAT";
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
        if (map == null || map.get(key) == null) {
            return null;
        }
        return String.valueOf(map.get(key));
    }

    private Integer getInteger(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
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
        if (map == null || map.get(key) == null) {
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
        if (map == null || map.get(key) == null) {
            return Map.of();
        }

        Object value = map.get(key);
        if (value instanceof Map<?, ?> nestedMap) {
            return (Map<String, Object>) nestedMap;
        }

        throw new IllegalArgumentException("Invalid nested object for key: " + key);
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