package com.dataworkshub.datalake.cdc;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for PostgresCdcSource.
 * Note: These are basic unit tests. Integration tests would require a running PostgreSQL instance.
 */
public class PostgresCdcSourceTest {
    private static final Logger LOG = LoggerFactory.getLogger(PostgresCdcSourceTest.class);

    @Test
    public void testCreatePostgresCdcSource() {
        // Create a PostgresCdcSource with test parameters
        PostgresCdcSource cdcSource = new PostgresCdcSource(
                "localhost",
                5432,
                "postgres",
                "public",
                "postgres",
                "postgres",
                new String[]{"public.customers", "public.orders"}
        );
        
        // Verify the source was created
        assertNotNull("CDC source should not be null", cdcSource);
        
        // Verify source function can be created
        assertNotNull("Source function should not be null", cdcSource.createSourceFunction());
        
        LOG.info("PostgresCdcSource created successfully");
    }
} 