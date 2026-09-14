package com.acme.salary.salary;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/employees/{employeeId}/salaries")
public class SalaryController {
 private final SalaryService service; SalaryController(SalaryService service){this.service=service;}
 public record Change(@DecimalMin("0.01") BigDecimal amount,@Pattern(regexp="[A-Z]{3}") String currencyCode,@PastOrPresent LocalDate effectiveFrom){}
 public record Response(Long id,BigDecimal amount,String currencyCode,LocalDate effectiveFrom,LocalDate effectiveTo){static Response from(Salary s){return new Response(s.id,s.amount,s.currencyCode,s.effectiveFrom,s.effectiveTo);}}
 @GetMapping List<Response> history(@PathVariable Long employeeId){return service.history(employeeId).stream().map(Response::from).toList();}
 @PostMapping Response add(@PathVariable Long employeeId,@Valid @RequestBody Change c){return Response.from(service.add(employeeId,c.amount(),c.currencyCode(),c.effectiveFrom()));}
}

