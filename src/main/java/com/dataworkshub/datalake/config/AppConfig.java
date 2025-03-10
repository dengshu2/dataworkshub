package com.dataworkshub.datalake.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Application configuration manager that loads settings from environment variables.
 */
public class AppConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);

    // PostgreSQL Configuration
    private final String pgHostname;
    private final int pgPort;
    private final String pgDatabase;
    private final String pgSchema;
    private final String pgUsername;
    private final String pgPassword;
    private final String[] pgTables;

    // Kafka Configuration
    private final String kafkaBootstrapServers;
    private final String kafkaTopic;
    private final String kafkaGroupId;

    /**
     * Constructor that loads configuration from environment variables.
     */
    public AppConfig() {
        // Load PostgreSQL configuration
        pgHostname = getEnvOrDefault("POSTGRES_HOST", "postgres");
        pgPort = Integer.parseInt(getEnvOrDefault("POSTGRES_PORT", "5432"));
        pgDatabase = getEnvOrDefault("POSTGRES_DB", "postgres");
        pgSchema = getEnvOrDefault("POSTGRES_SCHEMA", "public");
        pgUsername = getEnvOrDefault("POSTGRES_USER", "postgres");
        pgPassword = getEnvOrDefault("POSTGRES_PASSWORD", "postgres");
        
        // Parse comma-separated tables list
        String tablesStr = getEnvOrDefault("POSTGRES_TABLES", "public.customers,public.orders,public.order_items");
        pgTables = tablesStr.split(",");

        // Load Kafka configuration
        kafkaBootstrapServers = getEnvOrDefault("KAFKA_URL", "kafka:9092");
        kafkaTopic = getEnvOrDefault("KAFKA_TOPIC", "postgresql_cdc");
        kafkaGroupId = getEnvOrDefault("KAFKA_GROUP", "data_lake_group");
        
        // Log configuration
        LOG.info("Loaded configuration:");
        LOG.info("PostgreSQL: {}:{}/{}.{}", pgHostname, pgPort, pgDatabase, pgSchema);
        LOG.info("PostgreSQL Tables: {}", Arrays.toString(pgTables));
        LOG.info("Kafka: {}, Topic: {}, Group: {}", kafkaBootstrapServers, kafkaTopic, kafkaGroupId);
    }

    /**
     * Helper method to get environment variable with default fallback.
     *
     * @param name         Environment variable name
     * @param defaultValue Default value if environment variable is not set
     * @return Value of environment variable or default value
     */
    private String getEnvOrDefault(String name, String defaultValue) {
        String value = System.getenv(name);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }

    // Getters for PostgreSQL configuration
    public String getPgHostname() {
        return pgHostname;
    }

    public int getPgPort() {
        return pgPort;
    }

    public String getPgDatabase() {
        return pgDatabase;
    }

    public String getPgSchema() {
        return pgSchema;
    }

    public String getPgUsername() {
        return pgUsername;
    }

    public String getPgPassword() {
        return pgPassword;
    }

    public String[] getPgTables() {
        return pgTables;
    }

    // Getters for Kafka configuration
    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }

    public String getKafkaTopic() {
        return kafkaTopic;
    }

    public String getKafkaGroupId() {
        return kafkaGroupId;
    }
} 