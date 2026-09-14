package com.acme.salary;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.web.servlet.MockMvc;
@SpringBootTest @AutoConfigureMockMvc @Testcontainers(disabledWithoutDocker=true)
class DatabaseIntegrationTest {
 @Container static PostgreSQLContainer<?> postgres=new PostgreSQLContainer<>("postgres:17-alpine");
 @DynamicPropertySource static void database(DynamicPropertyRegistry r){r.add("spring.datasource.url",postgres::getJdbcUrl);r.add("spring.datasource.username",postgres::getUsername);r.add("spring.datasource.password",postgres::getPassword);r.add("app.seed.employee-count",()->10000);}
 @Autowired JdbcTemplate jdbc;
 @Autowired MockMvc mvc;
 @Test void migrationsAndSeedCreateExactlyTenThousandEmployees(){assertThat(jdbc.queryForObject("select count(*) from employee",Long.class)).isEqualTo(10_000);assertThat(jdbc.queryForObject("select count(*) from salary where effective_to is null",Long.class)).isEqualTo(10_000);}
 @Test void analyticsExposeDimensionsDistributionAndFilters() throws Exception {
  mvc.perform(get("/api/v1/analytics/summary").with(httpBasic("hr@acme.test","ChangeMe123!")))
      .andExpect(status().isOk()).andExpect(jsonPath("$.headcount").value(10_000))
      .andExpect(jsonPath("$.salaryDistribution.percentile25").isNumber())
      .andExpect(jsonPath("$.byCountry.length()").value(6))
      .andExpect(jsonPath("$.byDepartment.length()").value(8))
      .andExpect(jsonPath("$.byLevel.length()").value(5))
      .andExpect(jsonPath("$.byGender").isArray());
  mvc.perform(get("/api/v1/analytics/summary").param("country","CA")
          .param("departmentId","1").with(httpBasic("hr@acme.test","ChangeMe123!")))
      .andExpect(status().isOk()).andExpect(jsonPath("$.headcount").value(417));
 }
}
