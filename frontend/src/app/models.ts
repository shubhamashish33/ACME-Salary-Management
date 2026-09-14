export interface GroupMetric {
  label: string;
  headcount: number;
  averageSalary: number;
  payroll: number;
}

export interface Summary {
  headcount: number;
  annualPayroll: number;
  averageSalary: number;
  medianSalary: number;
  reportingCurrency: string;
  fxAsOf: string;
  salaryDistribution: SalaryDistribution;
  byCountry: GroupMetric[];
  byDepartment: GroupMetric[];
  byLevel: GroupMetric[];
  byGender: GroupMetric[];
}

export interface SalaryDistribution {
  minimum: number;
  percentile25: number;
  median: number;
  percentile75: number;
  maximum: number;
}

export interface DashboardFilters {
  country?: string;
  departmentId?: number;
  jobLevelId?: number;
  gender?: string;
}

export interface Employee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  gender: string;
  countryCode: string;
  departmentId: number;
  jobLevelId: number;
  hiredOn: string;
  active: boolean;
  version: number;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
}

export interface Salary {
  id: number;
  amount: number;
  currencyCode: string;
  effectiveFrom: string;
  effectiveTo: string | null;
}

export interface ReferenceOption {
  value: string;
  label: string;
}

export interface ImportPreview {
  totalRows: number;
  validRows: number;
  invalidRows: number;
  errors: { row: number; field: string; message: string }[];
}
