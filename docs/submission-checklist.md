# Submission checklist

This is the final evidence map for the assessment. Re-run the commands and replace the two pending items immediately before submission.

| Assessment expectation | Evidence | Status |
|---|---|---|
| Define requirements before building | `docs/requirements.md`; first repository commit `c2a0eaa` | Complete |
| HR salary management for 10,000 employees | Employee directory, effective-dated salary history, deterministic Flyway seed | Complete |
| Answer how the organization pays people | Filterable overview with payroll, average, median, percentiles, and country/department/level/gender comparisons | Complete |
| Full UI, backend, and relational database | Angular SPA, Spring Boot `/api/v1`, PostgreSQL/Flyway | Complete |
| Import spreadsheet data | Documented CSV template, validate-first atomic import, row errors, export | Complete |
| Fully deployed application | Render Blueprint, public web/API URLs, health check, cold-start guidance | Recheck after final push |
| Meaningful deterministic tests | Maven unit/API/PostgreSQL tests, Angular tests, seven Playwright smoke journeys | Complete |
| Clean architecture and trade-offs | `docs/architecture.md`, `docs/trade-offs.md`, capability packages | Complete |
| AI workflow | `docs/ai-workflow.md`, including assistance, verification, and rejected ideas | Complete |
| Incremental source history | Requirements-first and capability/fix-focused commits | Complete |
| Video demo | `docs/demo-script.md` provides the 5–7 minute walkthrough | Add unlisted URL to README |
| Repository link | GitHub remote is configured | Push local commits, then share URL |

## Final operator checks

1. Push the local commits together and wait for GitHub Actions and Render to finish.
2. Confirm the public health endpoint, login, overview filters, employee edit/salary history, CSV validation/import, and responsive popup behavior.
3. Record the demo using `docs/demo-script.md`, add the unlisted URL to the README, commit that URL, and push the final documentation commit.
4. Send the repository URL, application URL, video URL, and demo username. Share the Render-generated password privately; never commit it.
5. Record the Render PostgreSQL creation/expiry date and verify that it covers the review window.
