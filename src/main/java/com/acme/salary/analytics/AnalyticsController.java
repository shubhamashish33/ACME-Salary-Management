package com.acme.salary.analytics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/analytics")
public class AnalyticsController {
 private final JdbcTemplate jdbc; private final String reportingCurrency;
 AnalyticsController(JdbcTemplate jdbc,@Value("${app.reporting-currency}") String reportingCurrency){this.jdbc=jdbc;this.reportingCurrency=reportingCurrency;}
 public record Summary(long headcount,BigDecimal annualPayroll,BigDecimal averageSalary,BigDecimal medianSalary,String reportingCurrency,LocalDate fxAsOf,List<GroupMetric> byCountry,List<GroupMetric> byDepartment,List<GroupMetric> byLevel){}
 public record GroupMetric(String label,long headcount,BigDecimal averageSalary,BigDecimal payroll){}
 @GetMapping("/summary") Summary summary(){
   String base=" from employee e join salary s on s.employee_id=e.id and s.effective_to is null join fx_rate_set rs on rs.reporting_currency=? join fx_rate fx on fx.rate_set_id=rs.id and fx.currency_code=s.currency_code where e.active=true ";
   var row=jdbc.queryForMap("select count(*) headcount,coalesce(sum(s.amount/fx.units_per_reporting_currency),0) payroll,coalesce(avg(s.amount/fx.units_per_reporting_currency),0) average,coalesce(percentile_cont(0.5) within group(order by s.amount/fx.units_per_reporting_currency),0) median"+base,reportingCurrency);
   var asOf=jdbc.queryForObject("select max(as_of_date) from fx_rate_set where reporting_currency=?",LocalDate.class,reportingCurrency);
   return new Summary(((Number)row.get("headcount")).longValue(),decimal(row.get("payroll")),decimal(row.get("average")),decimal(row.get("median")),reportingCurrency,asOf,group("c.name", "country c", "c.code=e.country_code"),group("d.name", "department d", "d.id=e.department_id"),group("j.name", "job_level j", "j.id=e.job_level_id"));
 }
 private List<GroupMetric> group(String label,String dimension,String condition){
  var sql="select "+label+" label,count(*) headcount,avg(s.amount/fx.units_per_reporting_currency) average,sum(s.amount/fx.units_per_reporting_currency) payroll from employee e join "+dimension+" on "+condition+" join salary s on s.employee_id=e.id and s.effective_to is null join fx_rate_set rs on rs.reporting_currency=? join fx_rate fx on fx.rate_set_id=rs.id and fx.currency_code=s.currency_code where e.active=true group by "+label+" order by "+label;
  return jdbc.query(sql,(rs,n)->new GroupMetric(rs.getString("label"),rs.getLong("headcount"),rs.getBigDecimal("average"),rs.getBigDecimal("payroll")),reportingCurrency);
 }
 private static BigDecimal decimal(Object value){return value instanceof BigDecimal b?b:new BigDecimal(value.toString());}
}
