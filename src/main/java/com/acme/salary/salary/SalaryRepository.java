package com.acme.salary.salary;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface SalaryRepository extends JpaRepository<Salary,Long>{
 List<Salary> findByEmployeeIdOrderByEffectiveFromDesc(Long employeeId);
 @Query("select s from Salary s where s.employeeId=:employeeId and s.effectiveTo is null") Optional<Salary> findCurrent(@Param("employeeId") Long employeeId);
 boolean existsByEmployeeIdAndEffectiveFrom(Long employeeId,LocalDate date);
}

