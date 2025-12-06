package com.example.logtracing.serialization;

import com.example.logtracing.dto.TelemetryPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;
import org.msgpack.jackson.dataformat.MessagePackFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessagePackSerializer implements Serializer<TelemetryPayload> {

    private static final Logger logger = LoggerFactory.getLogger(MessagePackSerializer.class);
    private final ObjectMapper objectMapper = new ObjectMapper(new MessagePackFactory());

    @Override
    public byte[] serialize(String topic, TelemetryPayload data) {
        if (data == null) {
            return null;
        }

        try {
            byte[] bytes = objectMapper.writeValueAsBytes(data);
            logger.debug("Serialized message to MessagePack format: {} bytes", bytes.length);
            return bytes;
        } catch (Exception e) {
            logger.error("Error serializing message to MessagePack", e);
            throw new SerializationException("Error serializing message to MessagePack", e);
        }
    }
}

