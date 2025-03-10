package com.dataworkshub.datalake;

import com.dataworkshub.datalake.cdc.PostgresCdcSource;
import com.dataworkshub.datalake.config.AppConfig;
import com.dataworkshub.datalake.kafka.KafkaManager;
import org.apache.flink.api.common.RuntimeExecutionMode;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Data Lake Warehouse Application
 * The main entry point for the real-time data lake application.
 */
public class DataLakeApp {
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeApp.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Data Lake Warehouse Application");
        
        // Load application configuration
        AppConfig config = new AppConfig();
        
        // Set up the Flink execution environment
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setRuntimeMode(RuntimeExecutionMode.STREAMING);
        
        // Configure checkpointing for fault tolerance
        env.enableCheckpointing(60000); // Checkpoint every 60 seconds
        
        // Create Kafka sink
        KafkaManager kafkaManager = new KafkaManager(
                config.getKafkaBootstrapServers(),
                config.getKafkaTopic(),
                "cdc-transaction-"
        );
        KafkaSink<String> kafkaSink = kafkaManager.createCdcKafkaSink();
        
        // Create PostgreSQL CDC source
        PostgresCdcSource postgresCdcSource = new PostgresCdcSource(
                config.getPgHostname(),
                config.getPgPort(),
                config.getPgDatabase(),
                config.getPgSchema(),
                config.getPgUsername(),
                config.getPgPassword(),
                config.getPgTables()
        );
        
        // Create CDC stream and connect to Kafka
        DataStream<String> cdcStream = postgresCdcSource.createCdcStream(env, kafkaSink);
        
        // In future phases, we will add:
        // 1. Data transformation (Phase 3)
        // 2. Iceberg sink (Phase 4)
        
        LOG.info("Job configured, executing...");
        env.execute("Data Lake Warehouse Application");
    }
} 