#!/bin/bash
# Script to monitor Kafka topic for CDC events

# Default values
TOPIC=${1:-postgresql_cdc}
GROUP=${2:-monitor_group}

# Check if Kafka is running
echo "Checking Kafka status..."
if ! docker exec -it kafka kafka-topics.sh --bootstrap-server kafka:9092 --list > /dev/null 2>&1; then
    echo "Kafka is not running. Please start the containers first."
    exit 1
fi

echo "Kafka is running."

# List available topics
echo "Available Kafka topics:"
docker exec -it kafka kafka-topics.sh --bootstrap-server kafka:9092 --list

# Monitor the CDC topic
echo "Monitoring Kafka topic: $TOPIC"
echo "Press Ctrl+C to stop monitoring."
docker exec -it kafka kafka-console-consumer.sh \
    --bootstrap-server kafka:9092 \
    --topic $TOPIC \
    --group $GROUP \
    --from-beginning 