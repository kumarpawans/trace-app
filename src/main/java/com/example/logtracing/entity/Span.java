package com.example.logtracing.entity;

import jakarta.persistence.*;

@Entity
public class Span {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String spanId;
    private String parentSpanId;
    private String methodName;
    private String caller;
    private Long startTime;
    private Long endTime;
    @ManyToOne
    @JoinColumn(name = "trace_id")
    private Trace trace;
    // getters and setters
    public String getParentSpanId() { return parentSpanId; }
    public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getCaller() { return caller; }
    public void setCaller(String caller) { this.caller = caller; }
    public Long getStartTime() { return startTime; }
    public void setStartTime(Long startTime) { this.startTime = startTime; }
    public Long getEndTime() { return endTime; }
    public void setEndTime(Long endTime) { this.endTime = endTime; }
    public Trace getTrace() { return trace; }
    public void setTrace(Trace trace) { this.trace = trace; }
}
