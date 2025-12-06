package com.example.logtracing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TelemetryPayload {
    @JsonProperty("traceId")
    private String traceId;

    @JsonProperty("spanId")
    private String spanId;

    @JsonProperty("parentSpanId")
    private String parentSpanId;

    @JsonProperty("status")
    private String status; // "error" or "success"

    @JsonProperty("details")
    private String details;

    @JsonProperty("timestamp")
    private Long timestamp; // epoch millis

    // Default constructor for Jackson
    public TelemetryPayload() {
    }

    public TelemetryPayload(String traceId, String spanId, String parentSpanId, String status, String details, Long timestamp) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.parentSpanId = parentSpanId;
        this.status = status;
        this.details = details;
        this.timestamp = timestamp;
    }

    // Getters and setters
    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getSpanId() {
        return spanId;
    }

    public void setSpanId(String spanId) {
        this.spanId = spanId;
    }

    public String getParentSpanId() {
        return parentSpanId;
    }

    public void setParentSpanId(String parentSpanId) {
        this.parentSpanId = parentSpanId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "TelemetryPayload{" +
                "traceId='" + traceId + '\'' +
                ", spanId='" + spanId + '\'' +
                ", parentSpanId='" + parentSpanId + '\'' +
                ", status='" + status + '\'' +
                ", details='" + details + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}

