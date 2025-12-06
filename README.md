# Log Tracing Microservice

This Spring Boot microservice captures trace and span data for method calls (internal and external) using OpenTelemetry, Kafka, and PostgreSQL.

## Features
- REST endpoints for trace and span data
- **Kafka consumer** for ingesting telemetry data from `app-telemetry-log` topic
- **MessagePack compression** for efficient message serialization (20-40% smaller than JSON)
- **LZ4 compression** at Kafka level for optimal network performance
- PostgreSQL persistence
- OpenTelemetry integration for distributed tracing

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Kafka 2.8+ (optional, for Kafka consumer feature)

### Setup
1. Ensure PostgreSQL is running and accessible at `localhost:5432` with database `postgres` and user/password `postgres`.
2. (Optional) Start Kafka for telemetry ingestion:
   ```shell
   docker-compose up -d
   ```
3. Build the project:
   ```shell
   mvn clean install
   ```
4. Run the application:
   ```shell
   mvn spring-boot:run
   ```

## API Endpoints

### Trace and Span Endpoints
- `POST /api/log/trace-span` - Save trace and span directly to database
- `POST /api/log/publish-to-kafka` - Publish telemetry data to Kafka topic (with MessagePack compression)
- `POST /api/log/spans-by-trace` - Query spans by trace ID

### Example Request
```bash
curl -X POST http://localhost:4317/api/log/publish-to-kafka \
  -H "Content-Type: application/json" \
  -d '{
    "traceId": "trace-123",
    "spanId": "span-001",
    "parentSpanId": null,
    "status": "success",
    "details": "ServiceA.processRequest",
    "timestamp": 1733489400000
  }'
```

## Kafka Integration

### Consumer
The application includes a Kafka consumer that automatically ingests telemetry data from the `app-telemetry-log` topic.

**Key Features:**
- Automatic deserialization from MessagePack to Java objects
- Database persistence with source tracking (`source="kafka"`)
- Comprehensive error handling and logging

See [KAFKA_SETUP.md](KAFKA_SETUP.md) for detailed setup instructions.

### MessagePack Compression

Messages are compressed using **MessagePack** binary format, providing:
- **35-55% size reduction** compared to JSON
- **Faster serialization/deserialization**
- **Lower network bandwidth usage**
- **Combined with LZ4 compression** for optimal performance

**Example compression metrics:**
```
Original JSON size: 285 bytes
MessagePack size: 185 bytes (35% smaller)
MessagePack + LZ4: 120 bytes (58% smaller)
```

See [MESSAGEPACK_COMPRESSION.md](MESSAGEPACK_COMPRESSION.md) for detailed documentation.

## Configuration

### Database (application.properties)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=postgres
```

### Kafka (application.properties)
```properties
spring.kafka.bootstrap-servers=localhost:9092
spring.kafka.consumer.group-id=telemetry-consumer-group
spring.kafka.producer.compression-type=lz4
```

## Testing

### Run the MessagePack demo
```bash
./messagepack-demo.sh
```

### Test Kafka consumer
```bash
./test-kafka-consumer.sh
```

## OpenTelemetry
- Default exporter: OTLP
- Endpoint: `http://localhost:8090`

## Documentation

- [KAFKA_SETUP.md](KAFKA_SETUP.md) - Kafka consumer setup and usage
- [MESSAGEPACK_COMPRESSION.md](MESSAGEPACK_COMPRESSION.md) - MessagePack compression details

## Configuration
Edit `src/main/resources/application.properties` for database and OpenTelemetry settings.
