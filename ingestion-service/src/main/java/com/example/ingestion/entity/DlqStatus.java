package com.example.ingestion.entity;

public enum DlqStatus {
    FAILED,
    REPROCESSING,
    REPROCESSED_SUCCESS,
    REPROCESSED_FAILED
}