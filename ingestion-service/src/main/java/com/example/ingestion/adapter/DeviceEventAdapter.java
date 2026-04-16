package com.example.ingestion.adapter;

import java.util.Map;

import com.example.common.model.CommonEvent;

public interface DeviceEventAdapter {

    boolean supports(String vendorType);

    CommonEvent adapt(Map<String, Object> payload);
}