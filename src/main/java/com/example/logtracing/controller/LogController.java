
package com.example.logtracing.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.logtracing.entity.Trace;
import com.example.logtracing.entity.Span;
import com.example.logtracing.repository.TraceRepository;
import com.example.logtracing.repository.SpanRepository;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/log")
@CrossOrigin("*")
public class LogController {
    @Autowired
    private TraceRepository traceRepository;
    @Autowired
    private SpanRepository spanRepository;
    @PostMapping("/trace-span")
    public ResponseEntity<String> receiveTraceSpan(@RequestBody LogPayload payload) {
        // Find or create Trace
        Trace trace = traceRepository.findByTraceId(payload.traceId);
        if (trace == null) {
            trace = new Trace();
            trace.setTraceId(payload.traceId);
            trace.setSource("api"); // or set from payload if available
            trace = traceRepository.save(trace);
        }

        // Create and save Span
        Span span = new Span();
            span.setSpanId(payload.spanId);
            span.setParentSpanId(payload.parentSpanId);
            span.setMethodName(payload.details); // or set actual method name if available
            span.setCaller(payload.status); // or set actual caller if available
            span.setStartTime(payload.timestamp);
        span.setTrace(trace);
        spanRepository.save(span);

        return ResponseEntity.ok("Saved to DB");
    }

    public static class LogPayload {
        public String traceId;
        public String spanId;
        public String parentSpanId;
        public String status; // "error" or "success"
        public String details;
        public Long timestamp; // epoch millis
    }

    @PostMapping("/spans-by-trace")
    public ResponseEntity<?> getSpansByTrace(@RequestBody TraceQuery query) {
        // Find trace
        Trace trace = traceRepository.findByTraceId(query.traceId);
        if (trace == null || trace.getSpans() == null || trace.getSpans().isEmpty()) {
            // Default sample response
            return ResponseEntity.ok(java.util.Arrays.asList(
                new SpanSample(1L, "span-001", null, "ServiceA.processRequest", "success", 1701860000000L, null, new TraceSample(1L, "trace-123", "api")),
                new SpanSample(2L, "span-002", "span-001", "ServiceB.handleEvent", "success", 1701860000500L, null, new TraceSample(1L, "trace-123", "api")),
                new SpanSample(3L, "span-003", "span-002", "ServiceC.finalize", "error", 1701860001000L, null, new TraceSample(1L, "trace-123", "api"))
            ));
        }
        // Get spans, order by startTime
        var spans = trace.getSpans();
        spans.sort((a, b) -> Long.compare(
            a.getStartTime() != null ? a.getStartTime() : 0L,
            b.getStartTime() != null ? b.getStartTime() : 0L));
        return ResponseEntity.ok(spans);

    }

    // Sample DTOs for default response
    static class SpanSample {
        public Long id;
        public String spanId;
        public String parentSpanId;
        public String methodName;
        public String caller;
        public Long startTime;
        public Long endTime;
        public TraceSample trace;
        public SpanSample(Long id, String spanId, String parentSpanId, String methodName, String caller, Long startTime, Long endTime, TraceSample trace) {
            this.id = id;
            this.spanId = spanId;
            this.parentSpanId = parentSpanId;
            this.methodName = methodName;
            this.caller = caller;
            this.startTime = startTime;
            this.endTime = endTime;
            this.trace = trace;
        }
    }
    static class TraceSample {
        public Long id;
        public String traceId;
        public String source;
        public TraceSample(Long id, String traceId, String source) {
            this.id = id;
            this.traceId = traceId;
            this.source = source;
        }
    }
    static class TraceQuery {
        public String traceId;
    }
}
