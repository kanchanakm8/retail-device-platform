package com.example.ingestion.factory;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.ingestion.adapter.DeviceEventAdapter;

@Component
public class AdapterFactory {

    private final List<DeviceEventAdapter> adapters;

    public AdapterFactory(List<DeviceEventAdapter> adapters) {
        this.adapters = adapters;
    }

    public DeviceEventAdapter getAdapter(String vendorType) {
        return adapters.stream()
                .filter(adapter -> adapter.supports(vendorType))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported vendorType: " + vendorType));
    }
}