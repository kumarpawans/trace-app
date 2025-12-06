package com.example.logtracing.service;

import com.example.logtracing.dto.TelemetryPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper jsonMapper = new ObjectMapper();

    @Autowired
    private KafkaTemplate<String, TelemetryPayload> kafkaTemplate;

    public void sendTelemetryData(TelemetryPayload payload) {
        try {
            // Calculate original JSON size for comparison
            byte[] jsonBytes = jsonMapper.writeValueAsBytes(payload);
            int jsonSize = jsonBytes.length;

            CompletableFuture<SendResult<String, TelemetryPayload>> future =
                kafkaTemplate.send(TOPIC, payload.getTraceId(), payload);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    int serializedSize = result.getRecordMetadata().serializedValueSize();
                    double compressionRatio = (1 - (double) serializedSize / jsonSize) * 100;

                    logger.info("Sent telemetry message to topic: {} with offset: {} | " +
                               "Original JSON size: {} bytes, MessagePack size: {} bytes, " +
                               "Compression ratio: {:.2f}%",
                               TOPIC, result.getRecordMetadata().offset(),
                               jsonSize, serializedSize, compressionRatio);
                } else {
                    logger.error("Failed to send telemetry message: {}", ex.getMessage(), ex);
                }
            });
        } catch (Exception e) {
            logger.error("Error preparing telemetry message: {}", e.getMessage(), e);
        }
    }
}

