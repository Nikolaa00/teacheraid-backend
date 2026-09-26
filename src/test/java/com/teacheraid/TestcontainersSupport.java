package com.teacheraid;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Shared Testcontainers images. Spring Boot tests that need Postgres start {@link #postgres()}.
 */
public final class TestcontainersSupport {

    public static final DockerImageName POSTGRES =
            DockerImageName.parse("pgvector/pgvector:pg16").asCompatibleSubstituteFor("postgres");

    public static final DockerImageName REDIS = DockerImageName.parse("redis:7-alpine");

    public static PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>(POSTGRES)
                .withDatabaseName("teacheraid_dev")
                .withUsername("teacheraid")
                .withPassword("teacheraid");
    }

    @SuppressWarnings("resource")
    public static GenericContainer<?> redis() {
        return new GenericContainer<>(REDIS).withExposedPorts(6379);
    }

    private TestcontainersSupport() {}
}
