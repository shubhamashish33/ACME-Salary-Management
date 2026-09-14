package com.acme.salary.employee;

import java.util.Locale;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
class EmployeeReferenceValidator {
  private final JdbcTemplate jdbc;

  EmployeeReferenceValidator(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  void validate(String countryCode, Long departmentId, Long jobLevelId) {
    if (!exists("select exists(select 1 from country where code = ?)", countryCode.toUpperCase(Locale.ROOT))) {
      throw new IllegalArgumentException("Unknown country. Select a country from the available options.");
    }
    if (!exists("select exists(select 1 from department where id = ?)", departmentId)) {
      throw new IllegalArgumentException("Unknown department. Select a department from the available options.");
    }
    if (!exists("select exists(select 1 from job_level where id = ?)", jobLevelId)) {
      throw new IllegalArgumentException("Unknown job level. Select a job level from the available options.");
    }
  }

  private boolean exists(String sql, Object value) {
    return Boolean.TRUE.equals(jdbc.queryForObject(sql, Boolean.class, value));
  }
}
