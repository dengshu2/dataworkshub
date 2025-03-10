package com.dataworkshub.datalake.kafka;

import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for KafkaManager.
 * Note: These are basic unit tests. Integration tests would require a running Kafka instance.
 */
public class KafkaManagerTest {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaManagerTest.class);

    @Test
    public void testCreateKafkaSink() {
        // Create a KafkaManager with test parameters
        KafkaManager kafkaManager = new KafkaManager(
                "localhost:9092",
                "test_topic",
                "test-transaction-"
        );
        
        // Verify the manager was created
        assertNotNull("Kafka manager should not be null", kafkaManager);
        
        // Verify sink can be created
        KafkaSink<String> kafkaSink = kafkaManager.createCdcKafkaSink();
        assertNotNull("Kafka sink should not be null", kafkaSink);
        
        LOG.info("KafkaManager created sink successfully");
    }
} 