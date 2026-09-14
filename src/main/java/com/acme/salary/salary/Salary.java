package com.acme.salary.salary;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name="salary")
public class Salary {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
 @Column(name="employee_id",nullable=false) Long employeeId;
 @Column(nullable=false,precision=19,scale=2) BigDecimal amount;
 @Column(name="currency_code",nullable=false) String currencyCode;
 @Column(name="effective_from",nullable=false) LocalDate effectiveFrom;
 @Column(name="effective_to") LocalDate effectiveTo;
 @Column(name="created_at",insertable=false,updatable=false) OffsetDateTime createdAt;
 protected Salary(){}
 Salary(Long employeeId,BigDecimal amount,String currencyCode,LocalDate from){this.employeeId=employeeId;this.amount=amount;this.currencyCode=currencyCode;this.effectiveFrom=from;}
 void close(LocalDate date){effectiveTo=date;}
}

