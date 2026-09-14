CREATE TABLE department (id BIGSERIAL PRIMARY KEY, name VARCHAR(80) NOT NULL UNIQUE);
CREATE TABLE job_level (id BIGSERIAL PRIMARY KEY, code VARCHAR(20) NOT NULL UNIQUE, name VARCHAR(80) NOT NULL, rank_order INTEGER NOT NULL UNIQUE);
CREATE TABLE country (code CHAR(2) PRIMARY KEY, name VARCHAR(80) NOT NULL, currency_code CHAR(3) NOT NULL);
CREATE TABLE fx_rate_set (id BIGSERIAL PRIMARY KEY, as_of_date DATE NOT NULL UNIQUE, reporting_currency CHAR(3) NOT NULL);
CREATE TABLE fx_rate (rate_set_id BIGINT NOT NULL REFERENCES fx_rate_set(id), currency_code CHAR(3) NOT NULL, units_per_reporting_currency NUMERIC(19,6) NOT NULL CHECK (units_per_reporting_currency > 0), PRIMARY KEY(rate_set_id, currency_code));
CREATE TABLE employee (
  id BIGSERIAL PRIMARY KEY, employee_number VARCHAR(20) NOT NULL UNIQUE, first_name VARCHAR(80) NOT NULL,
  last_name VARCHAR(80) NOT NULL, email VARCHAR(160) NOT NULL UNIQUE, gender VARCHAR(20) NOT NULL,
  country_code CHAR(2) NOT NULL REFERENCES country(code), department_id BIGINT NOT NULL REFERENCES department(id),
  job_level_id BIGINT NOT NULL REFERENCES job_level(id), hired_on DATE NOT NULL, active BOOLEAN NOT NULL DEFAULT TRUE,
  version BIGINT NOT NULL DEFAULT 0, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE salary (
  id BIGSERIAL PRIMARY KEY, employee_id BIGINT NOT NULL REFERENCES employee(id), amount NUMERIC(19,2) NOT NULL CHECK(amount > 0),
  currency_code CHAR(3) NOT NULL, effective_from DATE NOT NULL, effective_to DATE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(), CHECK(effective_to IS NULL OR effective_to >= effective_from),
  UNIQUE(employee_id, effective_from)
);
CREATE TABLE import_run (id UUID PRIMARY KEY, file_name VARCHAR(255) NOT NULL, status VARCHAR(30) NOT NULL, total_rows INTEGER NOT NULL, valid_rows INTEGER NOT NULL, invalid_rows INTEGER NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT NOW());
CREATE INDEX idx_employee_filters ON employee(active, country_code, department_id, job_level_id);
CREATE INDEX idx_employee_name ON employee(last_name, first_name);
CREATE INDEX idx_salary_current ON salary(employee_id, effective_to);

