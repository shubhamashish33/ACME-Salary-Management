package com.acme.salary.salary;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.acme.salary.employee.EmployeeRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SalaryServiceTest {
 private SalaryRepository salaries; private EmployeeRepository employees; private SalaryService service;
 @BeforeEach void setUp(){salaries=mock(SalaryRepository.class);employees=mock(EmployeeRepository.class);service=new SalaryService(salaries,employees);when(employees.existsById(42L)).thenReturn(true);when(salaries.save(any())).thenAnswer(i->i.getArgument(0));}
 @Test void closesCurrentSalaryTheDayBeforeNewSalaryStarts(){
  var current=new Salary(42L,new BigDecimal("80000"),"USD",LocalDate.of(2024,1,1));when(salaries.findCurrent(42L)).thenReturn(Optional.of(current));
  var next=service.add(42L,new BigDecimal("90000"),"USD",LocalDate.of(2025,1,1));
  assertThat(current.effectiveTo).isEqualTo(LocalDate.of(2024,12,31));assertThat(next.amount).isEqualByComparingTo("90000");
 }
 @Test void rejectsAnOverlappingOrEarlierSalary(){
  var current=new Salary(42L,new BigDecimal("80000"),"USD",LocalDate.of(2025,1,1));when(salaries.findCurrent(42L)).thenReturn(Optional.of(current));
  assertThatThrownBy(()->service.add(42L,new BigDecimal("90000"),"USD",LocalDate.of(2024,6,1))).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("after the current");
 }
 @Test void rejectsNonPositiveSalary(){assertThatThrownBy(()->service.add(42L,BigDecimal.ZERO,"USD",LocalDate.now())).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("positive");}
}

