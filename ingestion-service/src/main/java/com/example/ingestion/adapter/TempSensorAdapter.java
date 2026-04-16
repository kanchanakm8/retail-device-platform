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
public class TempSensorAdapter implements DeviceEventAdapter {

    private static final double DEFAULT_MIN_TEMP = 2.0;
    private static final double DEFAULT_MAX_TEMP = 8.0;

    @Override
    public boolean supports(String vendorType) {
        return vendorType != null && "TEMP_SENSOR".equalsIgnoreCase(vendorType);
    }

    @Override
    public CommonEvent adapt(Map<String, Object> payload) {
        if (payload == null || payload.isEmpty()) {
            throw new IllegalArgumentException("TempSensor payload cannot be null or empty");
        }

        String sensorId = getString(payload, "sensorId");
        String storeId = getString(payload, "storeId");
        String zone = getString(payload, "zone");
        String coolerId = getString(payload, "coolerId");
        String unit = getString(payload, "unit");
        String status = getString(payload, "status");

        Double temperature = getDouble(payload, "temperature");
        Double humidity = getDouble(payload, "humidity");
        Integer batteryLevel = getInteger(payload, "batteryLevel");

        LocalDateTime timestamp = parseTimestamp(payload.get("timestamp"));

        if (sensorId == null || sensorId.isBlank()) {
            throw new IllegalArgumentException("sensorId is required");
        }

        if (temperature == null) {
            throw new IllegalArgumentException("temperature is required");
        }

        String normalizedUnit = (unit == null || unit.isBlank()) ? "C" : unit.toUpperCase();
        double temperatureInCelsius = convertToCelsius(temperature, normalizedUnit);

        String derivedReadingStatus = deriveReadingStatus(temperatureInCelsius, status);

        Map<String, Object> canonicalPayload = new HashMap<>();
        canonicalPayload.put("sensorId", sensorId);
        canonicalPayload.put("storeId", storeId);
        canonicalPayload.put("zone", zone);
        canonicalPayload.put("coolerId", coolerId);
        canonicalPayload.put("temperature", temperatureInCelsius);
        canonicalPayload.put("unit", "C");
        canonicalPayload.put("humidity", humidity);
        canonicalPayload.put("batteryLevel", batteryLevel);
        canonicalPayload.put("status", derivedReadingStatus);

        CommonEvent event = new CommonEvent();
        event.setEventId(UUID.randomUUID().toString());
        event.setDeviceId(sensorId);
        event.setEventType("TEMPERATURE_READING");
        event.setTimestamp(timestamp);
        event.setPayload(canonicalPayload);

        return event;
    }

    private String deriveReadingStatus(double temperatureInCelsius, String vendorStatus) {
        if (vendorStatus != null && !vendorStatus.isBlank()) {
            return vendorStatus;
        }

        if (temperatureInCelsius < DEFAULT_MIN_TEMP) {
            return "LOW_TEMPERATURE_ALERT";
        }

        if (temperatureInCelsius > DEFAULT_MAX_TEMP) {
            return "HIGH_TEMPERATURE_ALERT";
        }

        return "NORMAL";
    }

    private double convertToCelsius(double temperature, String unit) {
        if ("F".equalsIgnoreCase(unit)) {
            return (temperature - 32) * 5 / 9;
        }
        return temperature;
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

    private LocalDateTime parseTimestamp(Object timestampValue) {
        if (timestampValue == null) {
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