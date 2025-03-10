# Phase 2: CDC and Kafka Integration

This document describes the implementation of Phase 2 of the Data Lake Warehouse project, which focuses on Change Data Capture (CDC) from PostgreSQL and integration with Kafka.

## Components Implemented

### 1. PostgreSQL CDC Source

We've implemented a CDC source connector using Flink CDC for PostgreSQL. The `PostgresCdcSource` class:

- Configures and creates a PostgreSQL CDC source function
- Captures changes from specified tables in PostgreSQL
- Converts change events to JSON format
- Sends events to Kafka

Key files:
- `src/main/java/com/dataworkshub/datalake/cdc/PostgresCdcSource.java`

### 2. Kafka Integration

We've implemented Kafka integration to:

- Create a Kafka sink for CDC events
- Configure exactly-once delivery semantics
- Serialize CDC events as JSON

Key files:
- `src/main/java/com/dataworkshub/datalake/kafka/KafkaManager.java`

### 3. Configuration Management

We've implemented a configuration management system to:

- Load settings from environment variables
- Provide default values for missing configurations
- Make configuration accessible throughout the application

Key files:
- `src/main/java/com/dataworkshub/datalake/config/AppConfig.java`

### 4. CDC JSON Utilities

We've implemented utilities for working with CDC JSON events:

- Parse CDC JSON events
- Extract operation types (create, update, delete)
- Extract table names and record states
- Create simplified event representations

Key files:
- `src/main/java/com/dataworkshub/datalake/util/CdcJsonUtils.java`

## Testing and Verification

We've added several tools for testing and verifying the CDC setup:

1. **CDC Verification Script**: `scripts/verify-cdc.sh`
   - Checks PostgreSQL logical replication settings
   - Verifies replication slot creation
   - Confirms table existence

2. **Kafka Monitoring Script**: `scripts/monitor-kafka.sh`
   - Monitors the Kafka topic for CDC events
   - Displays CDC events in real-time

3. **CDC Test Commands**: Added to Makefile
   - `make test-cdc`: Performs insert, update, and delete operations to generate CDC events
   - `make verify-cdc`: Runs the CDC verification script
   - `make monitor-kafka`: Monitors the Kafka topic for CDC events

## How to Test

1. Start the environment:
   ```
   make up
   ```

2. Verify the CDC setup:
   ```
   make verify-cdc
   ```

3. In one terminal, monitor the Kafka topic:
   ```
   make monitor-kafka
   ```

4. In another terminal, generate CDC events:
   ```
   make test-cdc
   ```

5. Observe the CDC events in the Kafka monitoring terminal.

## Next Steps

In Phase 3, we will implement:

1. Data transformation and enrichment
2. Stream processing logic
3. Error handling and recovery mechanisms 