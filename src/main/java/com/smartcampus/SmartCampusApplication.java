package com.smartcampus;

import org.glassfish.jersey.server.ResourceConfig;
import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application entry point.
 * Sets the base URI for all API resources to /api/v1.
 *
 * Lifecycle note: By default, JAX-RS creates a new resource class instance
 * per request (per-request lifecycle). DataStore uses a singleton pattern
 * to ensure shared in-memory state is preserved across all requests.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends ResourceConfig {

    public SmartCampusApplication() {
        // Auto-scan packages for resources, providers and filters
        packages("com.smartcampus");
    }
}
