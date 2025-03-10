package com.dataworkshub.datalake.kafka;

import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * Manages Kafka sink configuration for CDC events.
 */
public class KafkaManager {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaManager.class);

    private final String bootstrapServers;
    private final String topic;
    private final String transactionIdPrefix;

    /**
     * Constructor for KafkaManager.
     *
     * @param bootstrapServers   Kafka bootstrap servers (comma-separated list)
     * @param topic              Kafka topic to write CDC events to
     * @param transactionIdPrefix Prefix for Kafka transaction IDs
     */
    public KafkaManager(String bootstrapServers, String topic, String transactionIdPrefix) {
        this.bootstrapServers = bootstrapServers;
        this.topic = topic;
        this.transactionIdPrefix = transactionIdPrefix;
    }

    /**
     * Creates a KafkaSink for writing CDC events.
     *
     * @return KafkaSink configured for writing string records
     */
    public KafkaSink<String> createCdcKafkaSink() {
        LOG.info("Creating Kafka sink for topic: {}", topic);
        
        // Create Kafka record serialization schema
        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema.<String>builder()
                .setTopic(topic)
                .setValueSerializationSchema(new SimpleStringSchema())
                .build();

        // Create producer properties
        Properties producerConfig = new Properties();
        producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        producerConfig.put(ProducerConfig.TRANSACTION_TIMEOUT_CONFIG, 15 * 60 * 1000); // 15 minutes
        producerConfig.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
        producerConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        producerConfig.put(ProducerConfig.RETRIES_CONFIG, Integer.toString(Integer.MAX_VALUE));
        
        // Create and return KafkaSink
        return KafkaSink.<String>builder()
                .setBootstrapServers(bootstrapServers)
                .setRecordSerializer(serializer)
                .setDeliveryGuarantee(DeliveryGuarantee.EXACTLY_ONCE)
                .setTransactionalIdPrefix(transactionIdPrefix)
                .setKafkaProducerConfig(producerConfig)
                .build();
    }
} 