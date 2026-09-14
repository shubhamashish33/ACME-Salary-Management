package com.acme.salary.employee;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;

class EmployeeReferenceValidatorTest {
  private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
  private final EmployeeReferenceValidator validator = new EmployeeReferenceValidator(jdbc);

  @Test
  void rejectsUnknownDepartmentBeforePersistence() {
    when(jdbc.queryForObject(anyString(), any(Class.class), any())).thenReturn(true, false);

    assertThatThrownBy(() -> validator.validate("US", 200L, 1L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown department");
  }

  @Test
  void rejectsUnknownJobLevelBeforePersistence() {
    when(jdbc.queryForObject(anyString(), any(Class.class), any())).thenReturn(true, true, false);

    assertThatThrownBy(() -> validator.validate("US", 1L, 307L))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unknown job level");
  }
}
