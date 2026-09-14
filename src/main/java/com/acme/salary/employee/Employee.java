package com.acme.salary.employee;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity @Table(name="employee")
public class Employee {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) Long id;
    @Column(name="employee_number", nullable=false, unique=true) String employeeNumber;
    @Column(name="first_name", nullable=false) String firstName;
    @Column(name="last_name", nullable=false) String lastName;
    @Column(nullable=false, unique=true) String email;
    @Column(nullable=false) String gender;
    @Column(name="country_code", nullable=false) String countryCode;
    @Column(name="department_id", nullable=false) Long departmentId;
    @Column(name="job_level_id", nullable=false) Long jobLevelId;
    @Column(name="hired_on", nullable=false) LocalDate hiredOn;
    @Column(nullable=false) boolean active = true;
    @Version long version;
    @Column(name="created_at", insertable=false, updatable=false) OffsetDateTime createdAt;
    @Column(name="updated_at", insertable=false) OffsetDateTime updatedAt;
    protected Employee() {}
    public Employee(String employeeNumber,String firstName,String lastName,String email,String gender,String countryCode,Long departmentId,Long jobLevelId,LocalDate hiredOn) {
        this.employeeNumber=employeeNumber; this.firstName=firstName; this.lastName=lastName; this.email=email; this.gender=gender; this.countryCode=countryCode; this.departmentId=departmentId; this.jobLevelId=jobLevelId; this.hiredOn=hiredOn;
    }
    void update(String firstName,String lastName,String email,String gender,String countryCode,Long departmentId,Long jobLevelId,LocalDate hiredOn) { this.firstName=firstName;this.lastName=lastName;this.email=email;this.gender=gender;this.countryCode=countryCode;this.departmentId=departmentId;this.jobLevelId=jobLevelId;this.hiredOn=hiredOn; }
    void deactivate(){ active=false; }
}

