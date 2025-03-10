.PHONY: build up down ps logs restart clean init-minio verify-cdc test-cdc monitor-kafka

# Build Docker image
build:
	docker-compose build

# Start all services
up:
	docker-compose up -d

# Stop all services
down:
	docker-compose down

# Show running containers
ps:
	docker-compose ps

# Show logs
logs:
	docker-compose logs -f

# Show logs for a specific service
logs-%:
	docker-compose logs -f $*

# Restart all services
restart:
	docker-compose restart

# Restart a specific service
restart-%:
	docker-compose restart $*

# Initialize MinIO buckets
init-minio:
	docker exec -it minio mkdir -p /data/warehouse
	docker exec -it minio mkdir -p /data/staging

# Verify CDC setup
verify-cdc:
	./scripts/verify-cdc.sh

# Test CDC by making changes to PostgreSQL
test-cdc:
	@echo "Inserting a new customer record..."
	docker exec -it postgres psql -U postgres -c "INSERT INTO customers (name, email) VALUES ('测试用户', 'test@example.com');"
	@echo "Updating an existing customer record..."
	docker exec -it postgres psql -U postgres -c "UPDATE customers SET name = '更新用户' WHERE email = 'test@example.com';"
	@echo "Deleting the test customer record..."
	docker exec -it postgres psql -U postgres -c "DELETE FROM customers WHERE email = 'test@example.com';"
	@echo "CDC test complete. Check Kafka topic for events."

# Monitor Kafka topic for CDC events
monitor-kafka:
	./scripts/monitor-kafka.sh

# Clean up resources
clean:
	docker-compose down -v
	docker system prune -f

# Create sample data
sample-data:
	docker exec -it postgres psql -U postgres -d postgres -f /docker-entrypoint-initdb.d/init-db.sql

# Help command
help:
	@echo "Available commands:"
	@echo "  make build        - Build Docker images"
	@echo "  make up           - Start all services"
	@echo "  make down         - Stop all services"
	@echo "  make ps           - Show running containers"
	@echo "  make logs         - Show logs for all services"
	@echo "  make logs-SERVICE - Show logs for a specific service"
	@echo "  make restart      - Restart all services"
	@echo "  make restart-SERVICE - Restart a specific service"
	@echo "  make init-minio   - Initialize MinIO buckets"
	@echo "  make verify-cdc   - Verify PostgreSQL CDC setup"
	@echo "  make test-cdc     - Test CDC by making changes to PostgreSQL"
	@echo "  make monitor-kafka - Monitor Kafka topic for CDC events"
	@echo "  make clean        - Clean up resources"
	@echo "  make sample-data  - Create sample data in PostgreSQL"
	@echo "  make help         - Show this help message" 