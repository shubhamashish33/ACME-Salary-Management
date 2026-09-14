package com.acme.salary.employee;
import java.util.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/employees")
public class EmployeeExportController {
 private final JdbcTemplate jdbc;EmployeeExportController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 @GetMapping(value="/export",produces="text/csv") ResponseEntity<String> export(){
  var out=new StringBuilder("employeeNumber,firstName,lastName,email,gender,countryCode,department,jobLevel,hiredOn,salary,currency,effectiveFrom\n");
  jdbc.query("select e.employee_number,e.first_name,e.last_name,e.email,e.gender,e.country_code,d.name,j.code,e.hired_on,s.amount,s.currency_code,s.effective_from from employee e join department d on d.id=e.department_id join job_level j on j.id=e.job_level_id join salary s on s.employee_id=e.id and s.effective_to is null where e.active=true order by e.employee_number",rs->{for(int i=1;i<=12;i++){if(i>1)out.append(',');out.append(csv(rs.getString(i)));}out.append('\n');});
  return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=employees.csv").body(out.toString());
 }
 private static String csv(String value){return value!=null&&(value.contains(",")||value.contains("\"")||value.contains("\n"))?'"'+value.replace("\"","\"\"")+'"':Objects.toString(value,"");}
}
