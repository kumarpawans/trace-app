package com.example.logtracing.serialization;

import com.example.logtracing.dto.TelemetryPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePackDeserializer implements Deserializer<TelemetryPayload> {

    private static final Logger logger = LoggerFactory.getLogger(MessagePackDeserializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

    @Override
    public TelemetryPayload deserialize(String topic, byte[] data) {
        if (data == null) {
            return null;
        }

        try {
            logger.debug("Deserializing MessagePack message: {} bytes", data.length);
            return objectMapper.readValue(data, TelemetryPayload.class);
        } catch (Exception e) {
            logger.error("Error deserializing MessagePack message", e);
            throw new SerializationException("Error deserializing MessagePack message", e);
        }
    }
}

