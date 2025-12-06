# Log Tracing Microservice

This Spring Boot microservice captures trace and span data for method calls (internal and external) using OpenTelemetry and PostgreSQL.

## Features
- REST endpoints for trace and span data
- PostgreSQL persistence
- OpenTelemetry integration for distributed tracing

## Setup
1. Ensure PostgreSQL is running and accessible at `localhost:5432` with database `logtracing` and user/password `postgres`.
2. Build the project:
   ```shell
   mvn clean install
   ```
3. Run the application:
   ```shell
   mvn spring-boot:run
   ```

## API Endpoints
- `POST /traces` - Create a trace
- `GET /traces` - List all traces
- `GET /traces/{traceId}` - Get trace by traceId
- `POST /spans` - Create a span
- `GET /spans` - List all spans
- `GET /spans/method/{methodName}` - Get spans by method name

## OpenTelemetry
- Default exporter: OTLP
- Endpoint: `http://localhost:4317`

## Configuration
Edit `src/main/resources/application.properties` for database and OpenTelemetry settings.
