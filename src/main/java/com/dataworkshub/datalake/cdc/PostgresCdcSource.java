package com.dataworkshub.datalake.cdc;

import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * PostgreSQL CDC Source connector for capturing database changes.
 */
public class PostgresCdcSource {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresCdcSource.class);

    private final String hostname;
    private final int port;
    private final String database;
    private final String schemaName;
    private final String username;
    private final String password;
    private final String[] tableList;

    /**
     * Constructor for PostgresCdcSource.
     *
     * @param hostname   PostgreSQL server hostname
     * @param port       PostgreSQL server port
     * @param database   PostgreSQL database name
     * @param schemaName PostgreSQL schema name
     * @param username   PostgreSQL username
     * @param password   PostgreSQL password
     * @param tableList  Array of tables to capture
     */
    public PostgresCdcSource(String hostname, int port, String database, String schemaName,
                            String username, String password, String[] tableList) {
        this.hostname = hostname;
        this.port = port;
        this.database = database;
        this.schemaName = schemaName;
        this.username = username;
        this.password = password;
        this.tableList = tableList;
    }

    /**
     * Creates a Flink CDC source function for PostgreSQL.
     *
     * @return SourceFunction that emits database changes as JSON strings
     */
    public SourceFunction<String> createSourceFunction() {
        Properties debeziumProperties = new Properties();
        // Set Debezium specific properties
        debeziumProperties.setProperty("snapshot.mode", "initial");
        debeziumProperties.setProperty("decimal.handling.mode", "string");
        debeziumProperties.setProperty("include.schema.changes", "true");
        
        // Create PostgreSQL CDC Source
        return PostgreSQLSource.<String>builder()
                .hostname(hostname)
                .port(port)
                .database(database)
                .schemaList(schemaName)
                .tableList(tableList)
                .username(username)
                .password(password)
                .decodingPluginName("pgoutput")
                .debeziumProperties(debeziumProperties)
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();
    }

    /**
     * Creates a DataStream of CDC events as JSON strings and sends them to Kafka.
     *
     * @param env       Flink StreamExecutionEnvironment
     * @param kafkaSink Kafka sink for writing CDC events
     * @return DataStream of CDC events as JSON strings
     */
    public DataStream<String> createCdcStream(StreamExecutionEnvironment env, KafkaSink<String> kafkaSink) {
        LOG.info("Creating PostgreSQL CDC stream from {}:{}/{}.{}", hostname, port, database, schemaName);
        
        // Create CDC source function
        SourceFunction<String> sourceFunction = createSourceFunction();
        
        // Create DataStream from source function
        DataStream<String> cdcStream = env.addSource(sourceFunction, "PostgreSQL-CDC-Source")
                .uid("postgres-cdc-source")
                .name("PostgreSQL CDC Source");
        
        // Add watermarks for event-time processing
        DataStream<String> cdcStreamWithWatermarks = cdcStream
                .assignTimestampsAndWatermarks(WatermarkStrategy.forMonotonousTimestamps());

        // Send CDC events to Kafka
        cdcStreamWithWatermarks.sinkTo(kafkaSink)
                .uid("kafka-cdc-sink")
                .name("Kafka CDC Sink");
        
        LOG.info("CDC stream created successfully");
        return cdcStreamWithWatermarks;
    }
} 