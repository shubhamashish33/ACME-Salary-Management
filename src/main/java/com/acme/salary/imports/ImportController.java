package com.acme.salary.imports;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import org.apache.commons.csv.*;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
@RestController @RequestMapping("/api/v1/imports")
public class ImportController {
 private static final List<String> HEADERS=List.of("employeeNumber","firstName","lastName","email","gender","countryCode","department","jobLevel","hiredOn","salary","currency","effectiveFrom");
 private final JdbcTemplate jdbc; ImportController(JdbcTemplate jdbc){this.jdbc=jdbc;}
 public record RowError(long row,String field,String message){}
 public record Preview(int totalRows,int validRows,int invalidRows,List<RowError> errors){}
 @PostMapping(value="/validate",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) Preview validate(@RequestPart("file") MultipartFile file)throws IOException{return parse(file).preview();}
 @PostMapping(value="/employees",consumes=MediaType.MULTIPART_FORM_DATA_VALUE) @Transactional Preview importEmployees(@RequestPart("file") MultipartFile file)throws IOException{
   var parsed=parse(file); if(parsed.preview.invalidRows()>0)throw new IllegalArgumentException("Import contains invalid rows; validate and correct it first");
   for(var r:parsed.rows){
    Long department=jdbc.queryForObject("select id from department where name=?",Long.class,r.get("department"));
    Long level=jdbc.queryForObject("select id from job_level where code=?",Long.class,r.get("jobLevel"));
    Long employeeId=jdbc.queryForObject("insert into employee(employee_number,first_name,last_name,email,gender,country_code,department_id,job_level_id,hired_on) values(?,?,?,?,?,?,?,?,?) returning id",Long.class,r.get("employeeNumber"),r.get("firstName"),r.get("lastName"),r.get("email"),r.get("gender"),r.get("countryCode"),department,level,Date.valueOf(r.get("hiredOn")));
    jdbc.update("insert into salary(employee_id,amount,currency_code,effective_from) values(?,?,?,?)",employeeId,new BigDecimal(r.get("salary")),r.get("currency"),Date.valueOf(r.get("effectiveFrom")));
   }
   var id=UUID.randomUUID(); jdbc.update("insert into import_run(id,file_name,status,total_rows,valid_rows,invalid_rows) values(?,?,?,?,?,0)",id,file.getOriginalFilename(),"COMPLETED",parsed.preview.totalRows(),parsed.preview.validRows()); return parsed.preview;
 }
 @GetMapping(value="/template",produces="text/csv") ResponseEntity<String> template(){return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,"attachment; filename=employees-template.csv").body(String.join(",",HEADERS)+"\nEMP-10001,Ada,Lovelace,ada@example.test,Female,GB,Engineering,L5,2020-01-15,120000,GBP,2024-01-01\n");}
 private Parsed parse(MultipartFile file)throws IOException{
   if(file.isEmpty())throw new IllegalArgumentException("Choose a non-empty CSV file"); var errors=new ArrayList<RowError>();var rows=new ArrayList<CSVRecord>();var seen=new HashSet<String>();
   try(var reader=new InputStreamReader(file.getInputStream(),StandardCharsets.UTF_8);var csv=CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).setTrim(true).get().parse(reader)){
    if(!csv.getHeaderNames().containsAll(HEADERS))throw new IllegalArgumentException("CSV must contain: "+String.join(", ",HEADERS));
    for(var r:csv){long line=r.getRecordNumber()+1; require(r,"employeeNumber",line,errors);require(r,"firstName",line,errors);require(r,"lastName",line,errors);require(r,"email",line,errors);
      if(!r.get("email").contains("@"))errors.add(new RowError(line,"email","Enter a valid email"));
      if(!seen.add(r.get("employeeNumber")))errors.add(new RowError(line,"employeeNumber","Duplicate in file"));
      try{LocalDate.parse(r.get("hiredOn"));LocalDate.parse(r.get("effectiveFrom"));new BigDecimal(r.get("salary"));}catch(Exception e){errors.add(new RowError(line,"dates/salary","Use ISO dates and a numeric salary"));}
      if(jdbc.queryForObject("select exists(select 1 from employee where employee_number=? or email=?)",Boolean.class,r.get("employeeNumber"),r.get("email")))errors.add(new RowError(line,"employeeNumber","Employee number or email already exists"));
      if(!jdbc.queryForObject("select exists(select 1 from country where code=?)",Boolean.class,r.get("countryCode")))errors.add(new RowError(line,"countryCode","Unknown country"));
      if(!jdbc.queryForObject("select exists(select 1 from department where name=?)",Boolean.class,r.get("department")))errors.add(new RowError(line,"department","Unknown department"));
      if(!jdbc.queryForObject("select exists(select 1 from job_level where code=?)",Boolean.class,r.get("jobLevel")))errors.add(new RowError(line,"jobLevel","Unknown job level")); rows.add(r);
    }
   }
   var bad=errors.stream().map(RowError::row).distinct().count();return new Parsed(rows,new Preview(rows.size(),(int)(rows.size()-bad),(int)bad,errors.stream().limit(100).toList()));
 }
 private static void require(CSVRecord r,String field,long line,List<RowError> errors){if(r.get(field).isBlank())errors.add(new RowError(line,field,"Required"));}
 private record Parsed(List<CSVRecord> rows,Preview preview){}
}

