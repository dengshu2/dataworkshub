package com.dataworkshub.datalake.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;

/**
 * Utility class for processing CDC JSON events.
 */
public class CdcJsonUtils {
    private static final Logger LOG = LoggerFactory.getLogger(CdcJsonUtils.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Parse a CDC JSON string into a JsonNode.
     *
     * @param cdcJson CDC event as JSON string
     * @return JsonNode representing the CDC event
     */
    public static JsonNode parseCdcJson(String cdcJson) {
        try {
            return OBJECT_MAPPER.readTree(cdcJson);
        } catch (IOException e) {
            LOG.error("Failed to parse CDC JSON: {}", cdcJson, e);
            return null;
        }
    }

    /**
     * Extract the operation type (c=create, u=update, d=delete) from a CDC event.
     *
     * @param cdcJson CDC event as JsonNode
     * @return Operation type as string
     */
    public static String getOperationType(JsonNode cdcJson) {
        if (cdcJson == null || !cdcJson.has("op")) {
            return "unknown";
        }
        return cdcJson.get("op").asText();
    }

    /**
     * Extract the table name from a CDC event.
     *
     * @param cdcJson CDC event as JsonNode
     * @return Table name as string
     */
    public static String getTableName(JsonNode cdcJson) {
        if (cdcJson == null || !cdcJson.has("source") || !cdcJson.get("source").has("table")) {
            return "unknown";
        }
        return cdcJson.get("source").get("table").asText();
    }

    /**
     * Extract the "after" state (new record state) from a CDC event.
     *
     * @param cdcJson CDC event as JsonNode
     * @return JsonNode representing the "after" state or null if not present
     */
    public static JsonNode getAfterState(JsonNode cdcJson) {
        if (cdcJson == null || !cdcJson.has("after")) {
            return null;
        }
        return cdcJson.get("after");
    }

    /**
     * Extract the "before" state (previous record state) from a CDC event.
     *
     * @param cdcJson CDC event as JsonNode
     * @return JsonNode representing the "before" state or null if not present
     */
    public static JsonNode getBeforeState(JsonNode cdcJson) {
        if (cdcJson == null || !cdcJson.has("before")) {
            return null;
        }
        return cdcJson.get("before");
    }

    /**
     * Create a simplified representation of a CDC event.
     *
     * @param cdcJson CDC event as JsonNode
     * @return Simplified JsonNode with key fields
     */
    public static JsonNode simplifyEvent(JsonNode cdcJson) {
        if (cdcJson == null) {
            return null;
        }

        ObjectNode simplified = OBJECT_MAPPER.createObjectNode();
        simplified.put("operation", getOperationType(cdcJson));
        simplified.put("table", getTableName(cdcJson));
        simplified.put("timestamp", System.currentTimeMillis());

        JsonNode before = getBeforeState(cdcJson);
        if (before != null) {
            simplified.set("before", before);
        }

        JsonNode after = getAfterState(cdcJson);
        if (after != null) {
            simplified.set("after", after);
        }

        return simplified;
    }

    /**
     * Convert a JsonNode to a JSON string.
     *
     * @param jsonNode JsonNode to convert
     * @return JSON string representation
     */
    public static String toJsonString(JsonNode jsonNode) {
        try {
            return OBJECT_MAPPER.writeValueAsString(jsonNode);
        } catch (IOException e) {
            LOG.error("Failed to convert JsonNode to string", e);
            return "{}";
        }
    }
} 