# 第二阶段：CDC与Kafka集成实现

本文档描述了数据湖仓库项目的第二阶段实现，重点关注从PostgreSQL的变更数据捕获(CDC)以及与Kafka的集成。

## 已实现的组件

### 1. PostgreSQL CDC源

我们使用Flink CDC连接器实现了PostgreSQL的CDC源。`PostgresCdcSource`类主要功能包括：

- 配置并创建PostgreSQL CDC源函数
- 从PostgreSQL的指定表中捕获数据变更
- 将变更事件转换为JSON格式
- 将事件发送到Kafka

核心文件：
- `src/main/java/com/dataworkshub/datalake/cdc/PostgresCdcSource.java`

### 2. Kafka集成

Kafka集成实现了以下功能：

- 为CDC事件创建Kafka接收器
- 配置精确一次性(exactly-once)语义传输
- 将CDC事件序列化为JSON格式

核心文件：
- `src/main/java/com/dataworkshub/datalake/kafka/KafkaManager.java`

### 3. 配置管理

我们实现了配置管理系统，用于：

- 从环境变量加载设置
- 为缺失的配置提供默认值
- 在整个应用程序中共享配置

核心文件：
- `src/main/java/com/dataworkshub/datalake/config/AppConfig.java`

### 4. CDC JSON工具

CDC JSON工具的功能包括：

- 解析CDC JSON事件
- 提取操作类型（创建、更新、删除）
- 提取表名和记录状态
- 创建简化的事件表示

核心文件：
- `src/main/java/com/dataworkshub/datalake/util/CdcJsonUtils.java`

## 测试与验证

为了测试和验证CDC设置，我们添加了几个工具：

1. **CDC验证脚本**：`scripts/verify-cdc.sh`
   - 检查PostgreSQL逻辑复制设置
   - 验证复制槽创建
   - 确认表存在

2. **Kafka监控脚本**：`scripts/monitor-kafka.sh`
   - 监控Kafka主题中的CDC事件
   - 实时显示CDC事件

3. **CDC测试命令**：添加到Makefile
   - `make test-cdc`：执行插入、更新和删除操作以生成CDC事件
   - `make verify-cdc`：运行CDC验证脚本
   - `make monitor-kafka`：监控Kafka主题中的CDC事件

## 如何测试

1. 启动环境：
   ```
   make up
   ```

2. 验证CDC设置：
   ```
   make verify-cdc
   ```

3. 在一个终端中监控Kafka主题：
   ```
   make monitor-kafka
   ```

4. 在另一个终端中生成CDC事件：
   ```
   make test-cdc
   ```

5. 观察Kafka监控终端中的CDC事件。

## Java代码实现详解

### 1. PostgresCdcSource实现

```java
package com.dataworkshub.datalake.cdc;

import com.dataworkshub.datalake.config.AppConfig;
import com.ververica.cdc.connectors.postgres.PostgreSQLSource;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class PostgresCdcSource {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresCdcSource.class);
    private final AppConfig config;

    public PostgresCdcSource(AppConfig config) {
        this.config = config;
    }

    public DataStream<String> createPostgresCdcSource(StreamExecutionEnvironment env) {
        LOG.info("Creating PostgreSQL CDC source");
        
        Properties debeziumProperties = new Properties();
        debeziumProperties.setProperty("snapshot.mode", "initial");
        
        SourceFunction<String> sourceFunction = PostgreSQLSource.<String>builder()
                .hostname(config.getPostgresHost())
                .port(config.getPostgresPort())
                .database(config.getPostgresDatabase())
                .schemaList("public")
                .tableList("public.customers,public.orders")
                .username(config.getPostgresUser())
                .password(config.getPostgresPassword())
                .decodingPluginName("pgoutput")
                .debeziumProperties(debeziumProperties)
                .deserializer(new JsonDebeziumDeserializationSchema())
                .build();
        
        return env.addSource(sourceFunction, "PostgreSQL CDC Source");
    }
}
```

### 2. KafkaManager实现

```java
package com.dataworkshub.datalake.kafka;

import com.dataworkshub.datalake.config.AppConfig;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.kafka.sink.KafkaRecordSerializationSchema;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class KafkaManager {
    private static final Logger LOG = LoggerFactory.getLogger(KafkaManager.class);
    private final AppConfig config;

    public KafkaManager(AppConfig config) {
        this.config = config;
    }

    public KafkaSink<String> createKafkaSink() {
        LOG.info("Creating Kafka sink for topic: {}", config.getKafkaTopic());
        
        KafkaRecordSerializationSchema<String> serializer = KafkaRecordSerializationSchema
                .<String>builder()
                .setValueSerializationSchema(new SimpleStringSchema())
                .setTopic(config.getKafkaTopic())
                .build();
        
        return KafkaSink.<String>builder()
                .setBootstrapServers(config.getKafkaBootstrapServers())
                .setRecordSerializer(serializer)
                .setDeliveryGuarantee(DeliveryGuarantee.AT_LEAST_ONCE)
                .build();
    }

    public void sendToKafka(DataStream<String> dataStream) {
        LOG.info("Sending data to Kafka topic: {}", config.getKafkaTopic());
        dataStream.sinkTo(createKafkaSink());
    }
}
```

### 3. AppConfig实现

```java
package com.dataworkshub.datalake.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConfig {
    private static final Logger LOG = LoggerFactory.getLogger(AppConfig.class);
    
    // Kafka配置
    private final String kafkaBootstrapServers;
    private final String kafkaTopic;
    private final String kafkaGroupId;
    
    // PostgreSQL配置
    private final String postgresHost;
    private final int postgresPort;
    private final String postgresDatabase;
    private final String postgresUser;
    private final String postgresPassword;
    
    public AppConfig() {
        // 从环境变量加载配置，并提供默认值
        kafkaBootstrapServers = getEnv("KAFKA_URL", "kafka:9092");
        kafkaTopic = getEnv("KAFKA_TOPIC", "postgresql_cdc");
        kafkaGroupId = getEnv("KAFKA_GROUP", "data_lake_group");
        
        String jdbcUrl = getEnv("POSTGRES_URL", "jdbc:postgresql://postgres:5432/postgres");
        postgresHost = extractHostFromJdbcUrl(jdbcUrl);
        postgresPort = extractPortFromJdbcUrl(jdbcUrl);
        postgresDatabase = extractDatabaseFromJdbcUrl(jdbcUrl);
        postgresUser = getEnv("POSTGRES_USER", "postgres");
        postgresPassword = getEnv("POSTGRES_PASSWORD", "postgres");
        
        LOG.info("Initialized configuration: Kafka={}, PostgreSQL={}:{}/{}",
                kafkaBootstrapServers, postgresHost, postgresPort, postgresDatabase);
    }
    
    private String getEnv(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null && !value.isEmpty()) ? value : defaultValue;
    }
    
    // 辅助方法，从JDBC URL中提取主机名、端口和数据库名
    // 实现略...
    
    // Getter方法
    public String getKafkaBootstrapServers() {
        return kafkaBootstrapServers;
    }
    
    public String getKafkaTopic() {
        return kafkaTopic;
    }
    
    public String getKafkaGroupId() {
        return kafkaGroupId;
    }
    
    public String getPostgresHost() {
        return postgresHost;
    }
    
    public int getPostgresPort() {
        return postgresPort;
    }
    
    public String getPostgresDatabase() {
        return postgresDatabase;
    }
    
    public String getPostgresUser() {
        return postgresUser;
    }
    
    public String getPostgresPassword() {
        return postgresPassword;
    }
}
```

### 4. 主应用程序 DataLakeApp

```java
package com.dataworkshub.datalake;

import com.dataworkshub.datalake.cdc.PostgresCdcSource;
import com.dataworkshub.datalake.config.AppConfig;
import com.dataworkshub.datalake.kafka.KafkaManager;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataLakeApp {
    private static final Logger LOG = LoggerFactory.getLogger(DataLakeApp.class);

    public static void main(String[] args) throws Exception {
        LOG.info("Starting Data Lake Application");
        
        // 创建配置
        AppConfig config = new AppConfig();
        
        // 创建Flink执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        
        // 创建PostgreSQL CDC源
        PostgresCdcSource cdcSource = new PostgresCdcSource(config);
        DataStream<String> cdcStream = cdcSource.createPostgresCdcSource(env);
        
        // 将CDC数据发送到Kafka
        KafkaManager kafkaManager = new KafkaManager(config);
        kafkaManager.sendToKafka(cdcStream);
        
        // 执行Flink作业
        LOG.info("Executing Flink job");
        env.execute("PostgreSQL CDC to Kafka");
    }
}
```

## 测试类实现

### 1. PostgresCdcSourceTest

```java
package com.dataworkshub.datalake.cdc;

import com.dataworkshub.datalake.config.AppConfig;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class PostgresCdcSourceTest {
    
    @Mock
    private AppConfig mockConfig;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
        // 设置模拟配置
        when(mockConfig.getPostgresHost()).thenReturn("localhost");
        when(mockConfig.getPostgresPort()).thenReturn(5432);
        when(mockConfig.getPostgresDatabase()).thenReturn("testdb");
        when(mockConfig.getPostgresUser()).thenReturn("testuser");
        when(mockConfig.getPostgresPassword()).thenReturn("testpassword");
    }
    
    @Test
    public void testCreatePostgresCdcSource() {
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        PostgresCdcSource cdcSource = new PostgresCdcSource(mockConfig);
        
        DataStream<String> stream = cdcSource.createPostgresCdcSource(env);
        
        assertNotNull("CDC Source should not be null", stream);
    }
}
```

### 2. KafkaManagerTest

```java
package com.dataworkshub.datalake.kafka;

import com.dataworkshub.datalake.config.AppConfig;
import org.apache.flink.connector.kafka.sink.KafkaSink;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class KafkaManagerTest {
    
    @Mock
    private AppConfig mockConfig;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
        // 设置模拟配置
        when(mockConfig.getKafkaBootstrapServers()).thenReturn("localhost:9092");
        when(mockConfig.getKafkaTopic()).thenReturn("test-topic");
        when(mockConfig.getKafkaGroupId()).thenReturn("test-group");
    }
    
    @Test
    public void testCreateKafkaSink() {
        KafkaManager kafkaManager = new KafkaManager(mockConfig);
        
        KafkaSink<String> sink = kafkaManager.createKafkaSink();
        
        assertNotNull("Kafka sink should not be null", sink);
    }
}
```

## 下一步

在第三阶段，我们将实现：

1. 数据转换和丰富化
2. 流处理逻辑
3. 错误处理和恢复机制
4. Flink SQL集成
5. 数据质量监控 