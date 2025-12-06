package com.example.logtracing.consumer;

import com.example.logtracing.dto.TelemetryPayload;
import com.example.logtracing.entity.Span;
import com.example.logtracing.entity.Trace;
import com.example.logtracing.repository.SpanRepository;
import com.example.logtracing.repository.TraceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
public class TelemetryKafkaConsumer {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryKafkaConsumer.class);

    @Autowired
    private TraceRepository traceRepository;

    @Autowired
    private SpanRepository spanRepository;

    @KafkaListener(topics = "app-telemetry-log", groupId = "telemetry-consumer-group")
    public void consumeTelemetryData(
            @Payload TelemetryPayload payload,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset) {

        try {
            logger.info("Received telemetry message from topic: {}, partition: {}, offset: {}",
                       topic, partition, offset);
            logger.info("Payload: {}", payload);

            // Validate payload
            if (payload.getTraceId() == null || payload.getSpanId() == null) {
                logger.error("Invalid payload: traceId or spanId is null");
                return;
            }

            // Find or create Trace
            Trace trace = traceRepository.findByTraceId(payload.getTraceId());
            if (trace == null) {
                trace = new Trace();
                trace.setTraceId(payload.getTraceId());
                trace.setSource("kafka");
                trace = traceRepository.save(trace);
                logger.info("Created new trace with traceId: {}", payload.getTraceId());
            } else {
                logger.info("Found existing trace with traceId: {}", payload.getTraceId());
            }

            // Create and save Span
            Span span = new Span();
            span.setSpanId(payload.getSpanId());
            span.setParentSpanId(payload.getParentSpanId());
            span.setMethodName(payload.getDetails());
            span.setCaller(payload.getStatus());
            span.setStartTime(payload.getTimestamp());
            span.setTrace(trace);
            spanRepository.save(span);

            logger.info("Successfully saved span with spanId: {} for traceId: {}",
                       payload.getSpanId(), payload.getTraceId());

        } catch (Exception e) {
            logger.error("Error processing telemetry data from Kafka: {}", e.getMessage(), e);
            // Depending on requirements, you might want to:
            // - Send to a dead-letter queue
            // - Retry with exponential backoff
            // - Just log and continue (current behavior)
        }
    }
}

