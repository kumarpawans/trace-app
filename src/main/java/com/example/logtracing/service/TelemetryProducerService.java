package com.example.logtracing.service;

import com.example.logtracing.dto.TelemetryPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TelemetryProducerService {

    private static final Logger logger = LoggerFactory.getLogger(TelemetryProducerService.class);
    private static final String TOPIC = "app-telemetry-log";

    @Autowired
    private KafkaTemplate<String, TelemetryPayload> kafkaTemplate;

    public void sendTelemetryData(TelemetryPayload payload) {
        CompletableFuture<SendResult<String, TelemetryPayload>> future =
            kafkaTemplate.send(TOPIC, payload.getTraceId(), payload);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                logger.info("Sent telemetry message to topic: {} with offset: {}",
                           TOPIC, result.getRecordMetadata().offset());
            } else {
                logger.error("Failed to send telemetry message: {}", ex.getMessage(), ex);
            }
        });
    }
}

