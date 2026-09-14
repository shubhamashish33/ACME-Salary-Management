package com.acme.salary.shared;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class SeedDataInitializer implements ApplicationRunner {
 private final JdbcTemplate jdbc; private final boolean enabled; private final int count;
 SeedDataInitializer(JdbcTemplate jdbc,@Value("${app.seed.enabled}") boolean enabled,@Value("${app.seed.employee-count}") int count){this.jdbc=jdbc;this.enabled=enabled;this.count=count;}
 @Override @Transactional public void run(ApplicationArguments args){
  if(!enabled || jdbc.queryForObject("select count(*) from employee",Long.class)>0)return;
  jdbc.update("""
   insert into employee(employee_number,first_name,last_name,email,gender,country_code,department_id,job_level_id,hired_on,active)
   select 'EMP-'||lpad(g::text,5,'0'), 'Employee'||g, 'Sample'||lpad(((g*37)%1000)::text,3,'0'),
     'employee'||g||'@example.test', (array['Female','Male','Non-binary'])[1+(g%3)],
     (array['US','IN','GB','DE','CA','SG'])[1+(g%6)], 1+(g%8), 1+(g%5), date '2014-01-01'+((g*17)%4380), true
   from generate_series(1,?) g
  """,count);
  jdbc.update("""
   insert into salary(employee_id,amount,currency_code,effective_from)
   select e.id,
     round((case c.currency_code when 'INR' then 1800000 when 'GBP' then 42000 when 'EUR' then 48000 when 'CAD' then 65000 when 'SGD' then 70000 else 60000 end
       * (1 + (j.rank_order-1)*0.42) * (0.88 + ((e.id*13)%25)/100.0))::numeric,2),
     c.currency_code, greatest(e.hired_on,date '2024-01-01')
   from employee e join country c on c.code=e.country_code join job_level j on j.id=e.job_level_id
  """);
 }
}

