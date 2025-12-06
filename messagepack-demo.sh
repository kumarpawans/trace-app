#!/bin/bash

# Script to demonstrate MessagePack compression benefits

echo "========================================="
echo "MessagePack Compression Demo"
echo "========================================="
echo ""

# Sample telemetry data
SAMPLE_DATA='{
  "traceId": "trace-550e8400-e29b-41d4-a716-446655440000",
  "spanId": "span-8f5e7a6b-3c2d-4e1f-9a7b-6c5d4e3f2a1b",
  "parentSpanId": "span-1a2b3c4d-5e6f-7a8b-9c0d-1e2f3a4b5c6d",
  "status": "success",
  "details": "OrderService.processPayment - Payment processed successfully for order #12345 with amount $199.99",
  "timestamp": 1733489400000
}'

# Calculate JSON size
JSON_SIZE=$(echo -n "$SAMPLE_DATA" | wc -c | tr -d ' ')
echo "Sample Telemetry Message:"
echo "$SAMPLE_DATA"
echo ""
echo "JSON Size (without whitespace): $(echo -n "$SAMPLE_DATA" | jq -c '.' | wc -c | tr -d ' ') bytes"
echo "JSON Size (with whitespace): $JSON_SIZE bytes"
echo ""

# Estimated MessagePack size (typically 30-40% smaller)
MSGPACK_EST=$((JSON_SIZE * 65 / 100))
MSGPACK_LZ4_EST=$((JSON_SIZE * 45 / 100))

echo "Estimated Sizes:"
echo "  MessagePack:        ~$MSGPACK_EST bytes (35% smaller)"
echo "  MessagePack + LZ4:  ~$MSGPACK_LZ4_EST bytes (55% smaller)"
echo ""

# Calculate annual savings for high-volume scenario
MSGS_PER_DAY=1000000
JSON_DAILY_MB=$((JSON_SIZE * MSGS_PER_DAY / 1024 / 1024))
MSGPACK_DAILY_MB=$((MSGPACK_LZ4_EST * MSGS_PER_DAY / 1024 / 1024))
SAVINGS_MB=$((JSON_DAILY_MB - MSGPACK_DAILY_MB))
SAVINGS_GB=$((SAVINGS_MB * 365 / 1024))

echo "========================================="
echo "Impact Analysis (1M messages/day)"
echo "========================================="
echo "Daily Data Volume:"
echo "  JSON:              ${JSON_DAILY_MB} MB/day"
echo "  MessagePack+LZ4:   ${MSGPACK_DAILY_MB} MB/day"
echo "  Daily Savings:     ${SAVINGS_MB} MB/day"
echo ""
echo "Annual Savings:      ~${SAVINGS_GB} GB/year"
echo ""

# Cost savings (assuming $0.10/GB for network egress)
COST_SAVINGS=$(echo "scale=2; $SAVINGS_GB * 0.10" | bc)
echo "Estimated Cost Savings (@ \$0.10/GB): ~\$${COST_SAVINGS}/year"
echo ""

echo "========================================="
echo "Test Your Setup"
echo "========================================="
echo ""
echo "Send a test message and check the logs for compression ratio:"
echo ""
echo "curl -X POST http://localhost:4317/api/log/publish-to-kafka \\"
echo "  -H 'Content-Type: application/json' \\"
echo "  -d '$SAMPLE_DATA'"
echo ""
echo "Then check application logs:"
echo "tail -f logs/application.log | grep 'Compression ratio'"
echo ""

