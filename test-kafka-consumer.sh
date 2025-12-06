#!/bin/bash

# Test script for Kafka Telemetry Consumer

echo "========================================="
echo "Kafka Telemetry Consumer Test Script"
echo "========================================="
echo ""

# Configuration
APP_URL="http://localhost:4317"
KAFKA_CONTAINER="kafka"

echo "Step 1: Check if Kafka is running..."
if docker ps | grep -q kafka; then
    echo "✓ Kafka is running"
else
    echo "✗ Kafka is not running. Please start Kafka first."
    echo "  You can use: docker-compose up -d"
    exit 1
fi

echo ""
echo "Step 2: Check if topic exists..."
docker exec $KAFKA_CONTAINER kafka-topics --list --bootstrap-server localhost:9092 | grep -q "app-telemetry-log"
if [ $? -eq 0 ]; then
    echo "✓ Topic 'app-telemetry-log' exists"
else
    echo "! Topic 'app-telemetry-log' does not exist. Creating it..."
    docker exec $KAFKA_CONTAINER kafka-topics --create --topic app-telemetry-log \
      --bootstrap-server localhost:9092 \
      --partitions 3 \
      --replication-factor 1
    echo "✓ Topic created"
fi

echo ""
echo "Step 3: Check if application is running..."
if curl -s -o /dev/null -w "%{http_code}" $APP_URL/api/log/spans-by-trace | grep -q "405\|200\|400"; then
    echo "✓ Application is running on port 4317"
else
    echo "✗ Application is not running. Please start it with: mvn spring-boot:run"
    exit 1
fi

echo ""
echo "Step 4: Publishing test message to Kafka..."
TIMESTAMP=$(date +%s)000
TEST_TRACE_ID="trace-test-$(date +%s)"
TEST_SPAN_ID="span-test-$(date +%s)"

RESPONSE=$(curl -s -X POST $APP_URL/api/log/publish-to-kafka \
  -H "Content-Type: application/json" \
  -d "{
    \"traceId\": \"$TEST_TRACE_ID\",
    \"spanId\": \"$TEST_SPAN_ID\",
    \"parentSpanId\": null,
    \"status\": \"success\",
    \"details\": \"TestService.execute\",
    \"timestamp\": $TIMESTAMP
  }")

echo "Response: $RESPONSE"

if [[ $RESPONSE == *"Message sent to Kafka"* ]]; then
    echo "✓ Message published to Kafka successfully"
else
    echo "✗ Failed to publish message to Kafka"
    exit 1
fi

echo ""
echo "Step 5: Waiting for consumer to process the message..."
sleep 3

echo ""
echo "Step 6: Querying the database for the trace..."
QUERY_RESPONSE=$(curl -s -X POST $APP_URL/api/log/spans-by-trace \
  -H "Content-Type: application/json" \
  -d "{\"traceId\": \"$TEST_TRACE_ID\"}")

echo "Query Response: $QUERY_RESPONSE"

if [[ $QUERY_RESPONSE == *"$TEST_SPAN_ID"* ]]; then
    echo "✓ Message was consumed and saved to database!"
    echo ""
    echo "========================================="
    echo "SUCCESS! Kafka consumer is working correctly."
    echo "========================================="
else
    echo "! Message might not have been consumed yet or there was an issue."
    echo "  Check application logs for details."
fi

echo ""
echo "Step 7: Monitoring consumer group..."
docker exec $KAFKA_CONTAINER kafka-consumer-groups \
  --bootstrap-server localhost:9092 \
  --group telemetry-consumer-group \
  --describe

echo ""
echo "Test completed!"

