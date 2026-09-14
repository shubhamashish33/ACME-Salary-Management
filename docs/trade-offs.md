# Trade-offs and production considerations

## Decisions

- **Modular monolith:** simpler deployment and transactions than microservices, while capability-based packages preserve boundaries.
- **PostgreSQL:** relational integrity and aggregate reporting fit employee and effective-dated salary data.
- **Salary history:** a new salary closes the previous period; history is never silently overwritten.
- **Fixed FX:** local amounts remain canonical, while normalized USD values use a versioned rate set for deterministic comparisons.
- **Curated analytics:** explicit, parameterized SQL is explainable, testable, and safer than natural-language SQL over sensitive information. Filters and descriptive breakdowns answer common questions; adjusted pay-equity or causal analysis is deliberately not claimed.
- **Basic authentication:** adequate as a visible assessment boundary for one demo HR user, but production would use SSO, short-lived sessions, audit logging, and granular authorization.
- **Atomic CSV import:** correctness and clear failure behavior are preferred over partial success. Large production imports would move to a queued job with progress and durable error artifacts.

## Performance and security

Lists are server-paginated and indexed. Analytics execute database aggregates rather than hydrating employees. The deterministic seed uses set-based SQL. Money uses `NUMERIC`, and the API uses validation, constrained CORS, non-root containers, environment secrets, and deactivation rather than deletion. Production should add rate limiting, audit events, secret rotation, CSP/security headers, backups, vulnerability scanning, and observability.

## Free hosting

Render keeps evaluation cost at zero but introduces cold starts and a 30-day database lifetime. That is acceptable for a time-boxed assessment only, not production. The recovery mechanism is Flyway plus deterministic idempotent seeding; Docker Compose supplies a stable local path.
