package com.acme.salary.employee;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    @Query("""
            select e from Employee e
            where
                (:active is null or e.active = :active)
                and (:country is null or e.countryCode = :country)
                and (:departmentId is null or e.departmentId = :departmentId)
                and (
                    :q is null
                    or lower(concat(e.firstName, ' ', e.lastName)) like lower(concat('%', cast(:q as string), '%'))
                    or lower(e.email) like lower(concat('%', cast(:q as string), '%'))
                    or lower(e.employeeNumber) like lower(concat('%', cast(:q as string), '%'))
                )
            """)
    Page<Employee> search(
            @Param("q") String q,
            @Param("country") String country,
            @Param("departmentId") Long departmentId,
            @Param("active") Boolean active,
            Pageable pageable);

    boolean existsByEmployeeNumberOrEmail(String employeeNumber, String email);
}