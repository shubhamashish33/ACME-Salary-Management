package com.acme.salary.analytics;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {
  private static final String DATASET = """
      from employee e
      join country c on c.code = e.country_code
      join department d on d.id = e.department_id
      join job_level j on j.id = e.job_level_id
      join salary s on s.employee_id = e.id and s.effective_to is null
      join fx_rate_set rs on rs.reporting_currency = :reportingCurrency
      join fx_rate fx on fx.rate_set_id = rs.id and fx.currency_code = s.currency_code
      where e.active = true
      """;

  private final NamedParameterJdbcTemplate jdbc;
  private final String reportingCurrency;

  AnalyticsController(NamedParameterJdbcTemplate jdbc,
      @Value("${app.reporting-currency}") String reportingCurrency) {
    this.jdbc = jdbc;
    this.reportingCurrency = reportingCurrency;
  }

  public record Summary(long headcount, BigDecimal annualPayroll, BigDecimal averageSalary,
      BigDecimal medianSalary, String reportingCurrency, LocalDate fxAsOf,
      Distribution salaryDistribution, List<GroupMetric> byCountry,
      List<GroupMetric> byDepartment, List<GroupMetric> byLevel, List<GroupMetric> byGender) {
  }

  public record Distribution(BigDecimal minimum, BigDecimal percentile25, BigDecimal median,
      BigDecimal percentile75, BigDecimal maximum) {
  }

  public record GroupMetric(String label, long headcount, BigDecimal averageSalary,
      BigDecimal payroll) {
  }

  @GetMapping("/summary")
  Summary summary(@RequestParam(required = false) String country,
      @RequestParam(required = false) Long departmentId,
      @RequestParam(required = false) Long jobLevelId,
      @RequestParam(required = false) String gender) {
    var query = filteredDataset(country, departmentId, jobLevelId, gender);
    var aggregate = jdbc.queryForMap("""
        select count(*) headcount,
          coalesce(sum(s.amount / fx.units_per_reporting_currency), 0) payroll,
          coalesce(avg(s.amount / fx.units_per_reporting_currency), 0) average,
          coalesce(min(s.amount / fx.units_per_reporting_currency), 0) minimum,
          coalesce(percentile_cont(0.25) within group(order by s.amount / fx.units_per_reporting_currency), 0) percentile25,
          coalesce(percentile_cont(0.5) within group(order by s.amount / fx.units_per_reporting_currency), 0) median,
          coalesce(percentile_cont(0.75) within group(order by s.amount / fx.units_per_reporting_currency), 0) percentile75,
          coalesce(max(s.amount / fx.units_per_reporting_currency), 0) maximum
        """ + query.sql(), query.params());
    var distribution = new Distribution(decimal(aggregate.get("minimum")),
        decimal(aggregate.get("percentile25")), decimal(aggregate.get("median")),
        decimal(aggregate.get("percentile75")), decimal(aggregate.get("maximum")));
    var asOf = jdbc.queryForObject("""
        select max(as_of_date) from fx_rate_set where reporting_currency = :reportingCurrency
        """, query.params(), LocalDate.class);

    return new Summary(number(aggregate, "headcount"), decimal(aggregate.get("payroll")),
        decimal(aggregate.get("average")), decimal(aggregate.get("median")), reportingCurrency,
        asOf, distribution, group("c.name", query), group("d.name", query),
        group("j.name", query), group("e.gender", query));
  }

  private FilteredQuery filteredDataset(String country, Long departmentId, Long jobLevelId,
      String gender) {
    var sql = new StringBuilder(DATASET);
    var params = new MapSqlParameterSource("reportingCurrency", reportingCurrency);
    if (country != null && !country.isBlank()) {
      sql.append(" and e.country_code = :country");
      params.addValue("country", country);
    }
    if (departmentId != null) {
      sql.append(" and e.department_id = :departmentId");
      params.addValue("departmentId", departmentId);
    }
    if (jobLevelId != null) {
      sql.append(" and e.job_level_id = :jobLevelId");
      params.addValue("jobLevelId", jobLevelId);
    }
    if (gender != null && !gender.isBlank()) {
      sql.append(" and e.gender = :gender");
      params.addValue("gender", gender);
    }
    return new FilteredQuery(sql.toString(), params);
  }

  private List<GroupMetric> group(String label, FilteredQuery query) {
    var sql = "select " + label + " label, count(*) headcount, "
        + "avg(s.amount / fx.units_per_reporting_currency) average, "
        + "sum(s.amount / fx.units_per_reporting_currency) payroll "
        + query.sql() + " group by " + label + " order by " + label;
    return jdbc.query(sql, query.params(), (resultSet, rowNumber) -> new GroupMetric(
        resultSet.getString("label"), resultSet.getLong("headcount"),
        resultSet.getBigDecimal("average"), resultSet.getBigDecimal("payroll")));
  }

  private static long number(Map<String, Object> row, String key) {
    return ((Number) row.get(key)).longValue();
  }

  private static BigDecimal decimal(Object value) {
    return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
  }

  private record FilteredQuery(String sql, MapSqlParameterSource params) {
  }
}
