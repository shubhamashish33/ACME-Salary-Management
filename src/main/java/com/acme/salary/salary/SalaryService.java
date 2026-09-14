package com.acme.salary.salary;
import com.acme.salary.employee.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class SalaryService {
 private final SalaryRepository salaries; private final EmployeeRepository employees;
 SalaryService(SalaryRepository salaries,EmployeeRepository employees){this.salaries=salaries;this.employees=employees;}
 public List<Salary> history(Long employeeId){requireEmployee(employeeId);return salaries.findByEmployeeIdOrderByEffectiveFromDesc(employeeId);}
 @Transactional public Salary add(Long employeeId,BigDecimal amount,String currency,LocalDate from){
   requireEmployee(employeeId); if(amount.signum()<=0) throw new IllegalArgumentException("Salary must be positive");
   if(from.isAfter(LocalDate.now())) throw new IllegalArgumentException("Future salary dates are not supported");
   if(salaries.existsByEmployeeIdAndEffectiveFrom(employeeId,from)) throw new IllegalArgumentException("A salary already starts on this date");
   salaries.findCurrent(employeeId).ifPresent(current->{if(!from.isAfter(current.effectiveFrom)) throw new IllegalArgumentException("New salary must start after the current salary");current.close(from.minusDays(1));salaries.save(current);});
   return salaries.save(new Salary(employeeId,amount,currency,from));
 }
 private void requireEmployee(Long id){if(!employees.existsById(id))throw new NoSuchElementException("Employee not found");}
}

