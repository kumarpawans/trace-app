package com.example.logtracing.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Trace {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String traceId;
    private String source;
    @OneToMany(mappedBy = "trace", cascade = CascadeType.ALL)
    private List<Span> spans;
    // getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public List<Span> getSpans() { return spans; }
    public void setSpans(List<Span> spans) { this.spans = spans; }
}
