package com.teacheraid;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class FlywaySchoolSchemaTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = TestcontainersSupport.postgres();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayCreatesSchoolRosterTables() {
        var tables = jdbcTemplate.queryForList(
                """
                        SELECT table_name
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN ('school', 'app_user', 'class', 'enrollment')
                        ORDER BY table_name
                        """,
                String.class);

        assertThat(tables).containsExactly("app_user", "class", "enrollment", "school");
    }
}
