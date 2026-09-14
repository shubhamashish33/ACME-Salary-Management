package com.acme.salary;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
@SpringBootTest @Testcontainers(disabledWithoutDocker=true)
class DatabaseIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);r.add("app.seed.employee-count",()->10000);}
 @Autowired JdbcTemplate jdbc;
 @Test void migrationsAndSeedCreateExactlyTenThousandEmployees(){assertThat(jdbc.queryForObject("select count(*) from employee",Long.class)).isEqualTo(10_000);assertThat(jdbc.queryForObject("select count(*) from salary where effective_to is null",Long.class)).isEqualTo(10_000);}
}
