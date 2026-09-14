package com.acme.salary.employee;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employees")
public class EmployeeController {
  private final EmployeeRepository repository;
  private final EmployeeReferenceValidator referenceValidator;

  public EmployeeController(EmployeeRepository repository, EmployeeReferenceValidator referenceValidator) {
    this.repository = repository;
    this.referenceValidator = referenceValidator;
  }

  public record Request(@NotBlank String employeeNumber, @NotBlank String firstName, @NotBlank String lastName,
      @Email @NotBlank String email, @NotBlank String gender, @NotBlank @Pattern(regexp = "[A-Z]{2}") String countryCode,
      @NotNull @Positive Long departmentId, @NotNull @Positive Long jobLevelId,
      @NotNull @PastOrPresent LocalDate hiredOn) {
  }

  public record Response(Long id, String employeeNumber, String firstName, String lastName, String email, String gender,
      String countryCode, Long departmentId, Long jobLevelId, LocalDate hiredOn, boolean active, long version) {
    static Response from(Employee e) {
      return new Response(e.id, e.employeeNumber, e.firstName, e.lastName, e.email, e.gender, e.countryCode,
          e.departmentId, e.jobLevelId, e.hiredOn, e.active, e.version);
    }
  }

  @GetMapping
  Page<Response> list(@RequestParam(required = false) String q, @RequestParam(required = false) String country,
      @RequestParam(required = false) Long departmentId, @RequestParam(required = false) Boolean active,
      @PageableDefault(size = 25, sort = "lastName") Pageable page) {
    return repository.search(blankToNull(q), blankToNull(country), departmentId, active, page).map(Response::from);
  }

  @GetMapping("/{id}")
  Response get(@PathVariable Long id) {
    return Response.from(find(id));
  }

  @PostMapping
  Response create(@Valid @RequestBody Request r) {
    if (repository.existsByEmployeeNumberOrEmail(r.employeeNumber(), r.email()))
      throw new IllegalArgumentException("Employee number or email already exists");
    referenceValidator.validate(r.countryCode(), r.departmentId(), r.jobLevelId());
    return Response.from(repository.save(new Employee(r.employeeNumber(), r.firstName(), r.lastName(), r.email(),
        r.gender(), r.countryCode(), r.departmentId(), r.jobLevelId(), r.hiredOn())));
  }

  @PutMapping("/{id}")
  Response update(@PathVariable Long id, @Valid @RequestBody Request r) {
    var e = find(id);
    referenceValidator.validate(r.countryCode(), r.departmentId(), r.jobLevelId());
    e.update(r.firstName(), r.lastName(), r.email(), r.gender(), r.countryCode(), r.departmentId(), r.jobLevelId(),
        r.hiredOn());
    return Response.from(repository.save(e));
  }

  @DeleteMapping("/{id}")
  void deactivate(@PathVariable Long id) {
    var e = find(id);
    e.deactivate();
    repository.save(e);
  }

  private Employee find(Long id) {
    return repository.findById(id).orElseThrow(() -> new NoSuchElementException("Employee not found"));
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }
}
