# ACME Salary Management

A web application for maintaining and understanding base salary data for 10,000 synthetic employees. The solution intentionally favors trustworthy, curated analytics over free-form AI queries.

## Product tour

- **Live application:** https://salary-management-web.onrender.com
- **Video walkthrough:** add the unlisted video URL before submission
- **Demo user:** `hr@acme.test`; obtain the generated password from the Render API environment
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
```

Core backend tests cover salary-period invariants and the authentication boundary. Frontend tests cover application creation and the login value proposition. PostgreSQL behavior is also exercised by the Docker smoke workflow and CI build.

## CSV contract

Download the canonical template from the Import page or `/api/v1/imports/template`. Required headers are:

```text
employeeNumber,firstName,lastName,email,gender,countryCode,department,jobLevel,hiredOn,salary,currency,effectiveFrom
```

Dates use ISO `YYYY-MM-DD`; country and currency values use ISO codes; department names and job-level codes must match reference data. Validation runs before import. An invalid file persists no employee rows, and duplicate employee numbers or emails are rejected.

## Deployment on Render

1. Push this repository to GitHub.
2. In Render, create a Blueprint from `render.yaml`.
3. Confirm the deployed hostnames still match the committed `API_URL` and `ALLOWED_ORIGINS`; update and redeploy if Render changes either hostname during recovery.
4. Copy the generated `DEMO_PASSWORD` into the submission instructions without committing it.
5. Check the health endpoint, wake the API, and complete the acceptance walkthrough before sharing the URL.

Free-tier limitations are material: the API sleeps after inactivity and can take about a minute to wake; the free PostgreSQL database expires 30 days after creation and has no backups. Record the database expiry date, deploy close to submission, and retain the deterministic seed/recovery path. Docker Compose remains the durable evaluator fallback.

## Architecture and decisions

The [product requirements](docs/requirements.md) were committed before application code. The [architecture note](docs/architecture.md) explains the modular monolith and data flow; [trade-offs](docs/trade-offs.md) records scope, security, performance, and free-hosting decisions; [AI workflow](docs/ai-workflow.md) records how assistance was used and verified.
