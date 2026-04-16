package com.example.ingestion.dto;

import java.util.Map;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DeviceEventRequest {

    @NotBlank(message = "vendorType is required")
    private String vendorType;

    @NotNull(message = "payload is required")
    private Map<String, Object> payload;

    public DeviceEventRequest() {
    }

    public DeviceEventRequest(String vendorType, Map<String, Object> payload) {
        this.vendorType = vendorType;
        this.payload = payload;
    }

    public String getVendorType() {
        return vendorType;
    }

    public void setVendorType(String vendorType) {
        this.vendorType = vendorType;
    }

    public Map<String, Object> getPayload() {
        return payload;
    }

    public void setPayload(Map<String, Object> payload) {
        this.payload = payload;
    }
}