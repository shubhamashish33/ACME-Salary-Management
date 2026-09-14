# ACME Salary Management

A web application for maintaining and understanding base salary data for 10,000 synthetic employees. The solution intentionally favors trustworthy, curated analytics over free-form AI queries.

The overview answers common organizational-pay questions with current active headcount, annual payroll, average and median salary, percentile distribution, and comparisons by country, department, job level, and gender. Filters are reflected in the URL so a view can be shared and reproduced. Gender results are descriptive and are not presented as an adjusted pay-equity analysis.

## Product tour

- **Live application:** https://salary-management-web.onrender.com
- **Video walkthrough:** 
- **Demo user:** `hr@acme.test`; 
- **API documentation:** https://salary-management-api-09al.onrender.com/swagger-ui/index.html
- **Health:** https://salary-management-api-09al.onrender.com/actuator/health

The hosted service uses synthetic data and fixed FX rates dated 2026-01-01. It does not contain current exchange rates or compensation recommendations.

## Run locally

Requirements: Docker Desktop with Compose.

```bash
docker compose up --build
```

Open `http://localhost:8081` and sign in with `hr@acme.test` / `ChangeMe123!`. The first start migrates PostgreSQL and deterministically seeds exactly 10,000 employees. To run the API and UI directly, use `mvn spring-boot:run` and `cd frontend && npm start` while PostgreSQL is available.

## Verify

```bash
mvn test
cd frontend
npm test -- --watch=false
npm run build
npm run e2e
```

Backend tests cover salary-period invariants, authentication/CORS, database migrations, seeding, reference validation, analytics, and filters. Angular tests cover core application behavior; Playwright smoke journeys cover dashboard questions and filters, employee lookup and editing, salary changes, deactivation behavior, CSV import, and responsive overlays. PostgreSQL behavior is exercised through Testcontainers in CI and through the Docker Compose acceptance workflow.

## CSV contract

Download the canonical template from the Import page or `/api/v1/imports/template`. Required headers are:

```text
employeeNumber,firstName,lastName,email,gender,countryCode,department,jobLevel,hiredOn,salary,currency,effectiveFrom
```

Dates use ISO `YYYY-MM-DD`; country and currency values use ISO codes; department names and job-level codes must match reference data. Validation runs before import. An invalid file persists no employee rows, and duplicate employee numbers or emails are rejected.


## Architecture and decisions

The [product requirements](docs/requirements.md) were committed before application code. The [architecture note](docs/architecture.md) explains the modular monolith and data flow; [trade-offs](docs/trade-offs.md) records scope, security, performance, and free-hosting decisions; [AI workflow](docs/ai-workflow.md) records how assistance was used and verified; and the [submission checklist](docs/submission-checklist.md) maps the assessment to evidence and remaining submission actions.
