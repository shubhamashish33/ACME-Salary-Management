package com.acme.salary.shared;
import java.util.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/reference")
public class ReferenceController {
 private final JdbcTemplate jdbc; ReferenceController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public record Option(String value,String label){}
 @GetMapping("/countries") List<Option> countries(){return jdbc.query("select code,name from country order by name",(r,n)->new Option(r.getString(1),r.getString(2)));}
 @GetMapping("/departments") List<Option> departments(){return jdbc.query("select id,name from department order by name",(r,n)->new Option(r.getString(1),r.getString(2)));}
 @GetMapping("/levels") List<Option> levels(){return jdbc.query("select id,name from job_level order by rank_order",(r,n)->new Option(r.getString(1),r.getString(2)));}
}

