# Salary Management - Product Requirements

## Goal and primary user

ACME currently manages salary information for 10,000 employees across multiple countries in spreadsheets. The primary user is an HR manager who needs one secure, web-based place to maintain employee base salaries and answer common questions about organizational pay. All included data is synthetic demonstration data.

## Success criteria

- HR can quickly find, filter, create, update, and deactivate employees without navigating spreadsheets.
- HR can validate and import a documented CSV format with actionable row-level feedback.
- Salary changes preserve previous values and effective dates rather than overwriting history.
- HR can compare headcount, payroll, average, median, and salary distributions across country, department, level, and gender.
- Cross-country figures show both local salary and a clearly labelled normalized reporting value using documented, deterministic exchange rates.
- The deployed application remains usable with 10,000 seeded employees and can be evaluated from its repository, public URL, local setup, tests, and video.

## Included in the assessment

- Server-paginated employee search, filters, sorting, creation, editing, and deactivation.
- Effective-dated base-salary history with non-overlapping periods.
- CSV preview, validation, duplicate detection, import, error reporting, and filtered export.
- ISO currencies, one reporting currency, and seeded exchange rates with an as-of date.
- Curated dashboard analytics by country, department, job level, and gender.
- A single HR-manager authentication boundary.
- Deterministic, idempotent generation of exactly 10,000 synthetic employees.

## Deliberate exclusions

| Exclusion | Reason |
|---|---|
| Bonus, equity, benefits | Base salary is sufficient for the core assessment; more components add breadth without improving the central workflow. |
| Payroll execution | The problem is salary-data management and analysis, not transferring money. |
| Multiple roles and SSO | One HR role is sufficient; enterprise identity integration would not prove additional core product value. |
| Employee self-service | It is outside the HR-manager persona. |
| Approval workflows | Useful later, but unnecessary to demonstrate the salary lifecycle. |
| Live exchange-rate integration | Seeded rates are deterministic, auditable, and explicitly permitted. |
| AI or natural-language querying | Curated analytics are safer, explainable, and testable for sensitive compensation data. |
| Predictive pay recommendations | These require policy, bias, and governance decisions beyond this assessment. |
| Hard deletion | Deactivation preserves business history and referential integrity. |

Future considerations include SSO and granular authorization, audit events, approval workflows, broader compensation components, governed live exchange rates, saved reports, and deeper accessibility and observability work.
