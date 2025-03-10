# Source Code Directory

This directory contains the Java source code for the Data Lake Warehouse application.

## Project Structure

The project is organized into the following packages:

- `com.dataworkshub.datalake`: Main application package
  - `cdc`: PostgreSQL CDC related code (Phase 2)
  - `kafka`: Kafka integration (Phase 2)
  - `flink`: Flink transformations and processing (Phase 3)
  - `iceberg`: Iceberg integration (Phase 4)

## Development Phases

The source code will be developed according to the following phases:

### Phase 1: Environment Setup

- Project structure and configuration
- Basic application skeleton

### Phase 2: CDC and Kafka Integration

- PostgreSQL CDC connector
- Kafka producer/consumer setup
- Schema registry integration

### Phase 3: Data Processing

- Data transformation and enrichment
- Stream processing logic
- Error handling and recovery

### Phase 4: Iceberg Integration

- Iceberg table schema management
- Data writing to Iceberg
- Time travel and snapshot management

### Phase 5: Analytics and Monitoring

- Analytics queries
- Monitoring and observability
- Dashboard integration 