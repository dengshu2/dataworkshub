#!/bin/bash
# Script to verify PostgreSQL CDC setup

# Check if PostgreSQL is running
echo "Checking PostgreSQL status..."
if ! docker exec -it postgres pg_isready -U postgres > /dev/null 2>&1; then
    echo "PostgreSQL is not running. Please start the containers first."
    exit 1
fi

echo "PostgreSQL is running."

# Check logical replication settings
echo "Checking logical replication settings..."
docker exec -it postgres psql -U postgres -c "SHOW wal_level;"
docker exec -it postgres psql -U postgres -c "SHOW max_replication_slots;"
docker exec -it postgres psql -U postgres -c "SHOW max_wal_senders;"

# Check if tables exist
echo "Checking if tables exist..."
docker exec -it postgres psql -U postgres -c "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public';"

# Create a replication slot for testing
echo "Creating a test replication slot..."
docker exec -it postgres psql -U postgres -c "SELECT pg_create_logical_replication_slot('test_slot', 'pgoutput');"

# List replication slots
echo "Listing replication slots..."
docker exec -it postgres psql -U postgres -c "SELECT * FROM pg_replication_slots;"

# Drop the test replication slot
echo "Dropping test replication slot..."
docker exec -it postgres psql -U postgres -c "SELECT pg_drop_replication_slot('test_slot');"

echo "CDC verification complete." 