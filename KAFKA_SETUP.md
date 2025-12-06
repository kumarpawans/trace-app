# Kafka Telemetry Consumer Setup

## Overview
This application includes a Kafka consumer that ingests telemetry data (traces and spans) from the `app-telemetry-log` topic and persists them to the database.

## Components

### 1. Kafka Consumer
- **Class**: `TelemetryKafkaConsumer`
- **Location**: `src/main/java/com/example/logtracing/consumer/TelemetryKafkaConsumer.java`
- **Topic**: `app-telemetry-log`
- **Consumer Group**: `telemetry-consumer-group`

### 2. Kafka Producer Service
- **Class**: `TelemetryProducerService`
- **Location**: `src/main/java/com/example/logtracing/service/TelemetryProducerService.java`
- **Purpose**: Sends telemetry data to the Kafka topic

### 3. Configuration
- **Consumer Config**: `KafkaConsumerConfig.java`
- **Producer Config**: `KafkaProducerConfig.java`
- **Properties**: `application.properties`

### 4. DTOs
- **TelemetryPayload**: Data transfer object for telemetry messages

## Setup Instructions

### 1. Start Kafka (if not already running)

Using Docker:
```bash
# Start Zookeeper
docker run -d --name zookeeper -p 2181:2181 zookeeper:3.8

# Start Kafka
docker run -d --name kafka -p 9092:9092 \
  -e KAFKA_ZOOKEEPER_CONNECT=host.docker.internal:2181 \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://localhost:9092 \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  confluentinc/cp-kafka:latest
```

Or using Docker Compose (create a `docker-compose.yml`):
```yaml
version: '3'
services:
  zookeeper:
    image: zookeeper:3.8
    ports:
      - "2181:2181"
  
  kafka:
    image: confluentinc/cp-kafka:latest
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
```

Then run:
```bash
docker-compose up -d
```

### 2. Create Kafka Topic

```bash
# Using kafka-topics.sh (if Kafka is installed locally)
kafka-topics.sh --create --topic app-telemetry-log \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1

# Or using Docker exec
docker exec -it kafka kafka-topics --create --topic app-telemetry-log \
  --bootstrap-server localhost:9092 \
  --partitions 3 \
  --replication-factor 1
```

### 3. Start the Application

```bash
mvn spring-boot:run
```

## API Endpoints

### 1. Publish Telemetry Data to Kafka
**Endpoint**: `POST /api/log/publish-to-kafka`

**Request Body**:
```json
{
  "traceId": "trace-123",
  "spanId": "span-001",
  "parentSpanId": null,
  "status": "success",
  "details": "ServiceA.processRequest",
  "timestamp": 1701860000000
}
```

**Response**:
```
Message sent to Kafka topic: app-telemetry-log
```

### 2. Save Directly to DB (bypasses Kafka)
**Endpoint**: `POST /api/log/trace-span`

**Request Body**: Same as above

**Response**:
```
Saved to DB
```

### 3. Query Spans by Trace
**Endpoint**: `POST /api/log/spans-by-trace`

**Request Body**:
```json
{
  "traceId": "trace-123"
}
```

## Testing the Kafka Consumer

### Using cURL:

```bash
# Publish telemetry data to Kafka
curl -X POST http://localhost:4317/api/log/publish-to-kafka \
  -H "Content-Type: application/json" \
  -d '{
    "traceId": "trace-test-001",
    "spanId": "span-test-001",
    "parentSpanId": null,
    "status": "success",
    "details": "TestService.execute",
    "timestamp": 1733489400000
  }'

# Query the saved spans
curl -X POST http://localhost:4317/api/log/spans-by-trace \
  -H "Content-Type: application/json" \
  -d '{
    "traceId": "trace-test-001"
  }'
```

### Using kafka-console-producer:

```bash
# Produce messages directly to Kafka topic
docker exec -it kafka kafka-console-producer \
  --topic app-telemetry-log \
  --bootstrap-server localhost:9092

# Then paste JSON messages:
{"traceId":"trace-123","spanId":"span-001","parentSpanId":null,"status":"success","details":"ServiceA.processRequest","timestamp":1701860000000}
```

### Using kafka-console-consumer (to verify messages):

```bash
# Consume messages from the topic
docker exec -it kafka kafka-console-consumer \
  --topic app-telemetry-log \
  --bootstrap-server localhost:9092 \
  --from-beginning
```

## Configuration Properties

Update `application.properties` if you need to change Kafka settings:

```properties
# Kafka Bootstrap Servers
spring.kafka.bootstrap-servers=localhost:9092

# Consumer Configuration
spring.kafka.consumer.group-id=telemetry-consumer-group
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.JsonDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
spring.kafka.consumer.properties.spring.json.value.default.type=com.example.logtracing.dto.TelemetryPayload
```

## How It Works

1. **Producer Flow**:
   - Client sends telemetry data to `/api/log/publish-to-kafka`
   - `LogController` receives the request
   - `TelemetryProducerService` publishes the message to `app-telemetry-log` topic
   - Message is queued in Kafka

2. **Consumer Flow**:
   - `TelemetryKafkaConsumer` listens to `app-telemetry-log` topic
   - When a message arrives, the consumer deserializes it to `TelemetryPayload`
   - Consumer finds or creates a `Trace` entity with source="kafka"
   - Consumer creates and saves a `Span` entity linked to the trace
   - Data is persisted to PostgreSQL database

3. **Direct DB Flow** (for comparison):
   - Client sends data to `/api/log/trace-span`
   - Data is saved directly to DB with source="api"

## Monitoring

Check application logs for consumer activity:

```bash
tail -f logs/application.log | grep TelemetryKafkaConsumer
```

You should see messages like:
```
Received telemetry message from topic: app-telemetry-log, partition: 0, offset: 123
Payload: TelemetryPayload{traceId='trace-123', spanId='span-001', ...}
Created new trace with traceId: trace-123
Successfully saved span with spanId: span-001 for traceId: trace-123
```

## Troubleshooting

### Consumer not receiving messages
1. Verify Kafka is running: `docker ps | grep kafka`
2. Check topic exists: `docker exec kafka kafka-topics --list --bootstrap-server localhost:9092`
3. Check consumer group: `docker exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --group telemetry-consumer-group --describe`

### Connection refused errors
- Ensure `spring.kafka.bootstrap-servers` matches your Kafka address
- If using Docker, ensure ports are correctly mapped

### Deserialization errors
- Verify the JSON structure matches `TelemetryPayload` class
- Check that all required fields are present in the message

## Production Considerations

1. **Error Handling**: Configure a Dead Letter Topic (DLT) for failed messages
2. **Scaling**: Increase the number of partitions and consumer instances
3. **Monitoring**: Integrate with tools like Kafka Manager or Confluent Control Center
4. **Security**: Enable SSL/TLS and SASL authentication for production
5. **Performance**: Tune consumer properties like `max.poll.records`, `fetch.min.bytes`, etc.

