import { CommonModule, DatePipe, DecimalPipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, HttpHeaders } from '@angular/common/http';
import { Component, Input, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { IconComponent } from '../icon.component';
import { Employee, Page, ReferenceOption, Salary } from '../models';

@Component({
  selector: 'app-employee-directory',
  standalone: true,
  imports: [CommonModule, FormsModule, DatePipe, DecimalPipe, IconComponent],
  templateUrl: './employee-directory.component.html',
  styleUrl: './employee-directory.component.scss',
})
export class EmployeeDirectoryComponent implements OnInit {
  @Input({ required: true }) api = '';
  @Input({ required: true }) auth!: HttpHeaders;

  employees = signal<Page<Employee> | null>(null);
  selected = signal<Employee | null>(null);
  salaries = signal<Salary[]>([]);
  departments = signal<ReferenceOption[]>([]);
  levels = signal<ReferenceOption[]>([]);
  countries = signal<ReferenceOption[]>([]);
  editing = signal(false);
  loading = signal(false);
  error = signal('');

  employeeDraft: Partial<Employee> = {};
  query = '';
  active = 'true';
  page = 0;
  amount?: number;
  currency = 'USD';
  effectiveFrom = new Date().toISOString().slice(0, 10);

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.loadReferences();
    this.loadEmployees();
  }

  private options() {
    return { headers: this.auth };
  }

  loadReferences(): void {
    forkJoin({
      countries: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/countries`, this.options()),
      departments: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/departments`, this.options()),
      levels: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/levels`, this.options()),
    }).subscribe({
      next: ({ countries, departments, levels }) => {
        this.countries.set(countries);
        this.departments.set(departments);
        this.levels.set(levels);
      },
      error: (error) => this.fail(error, 'Could not load employee form options.'),
    });
  }

  loadEmployees(page = this.page): void {
    this.loading.set(true);
    this.error.set('');
    this.page = page;
    const params: Record<string, string | number> = { page, size: 25, sort: 'lastName,asc', active: this.active };
    if (this.query.trim()) params['q'] = this.query.trim();
    this.http.get<Page<Employee>>(`${this.api}/api/v1/employees`, { ...this.options(), params }).subscribe({
      next: (value) => { this.employees.set(value); this.loading.set(false); },
      error: (error) => this.fail(error, 'Could not load employees.'),
    });
  }

  selectEmployee(employee: Employee): void {
    this.selected.set(employee);
    this.editing.set(false);
    this.employeeDraft = { ...employee };
    this.amount = undefined;
    this.http.get<Salary[]>(`${this.api}/api/v1/employees/${employee.id}/salaries`, this.options()).subscribe({
      next: (value) => this.salaries.set(value),
      error: (error) => this.fail(error, 'Could not load salary history.'),
    });
  }

  openCreate(): void {
    this.selected.set(null);
    this.employeeDraft = {
      employeeNumber: '', firstName: '', lastName: '', email: '', gender: 'Prefer not to say',
      countryCode: this.countries()[0]?.value ?? '',
      departmentId: Number(this.departments()[0]?.value),
      jobLevelId: Number(this.levels()[0]?.value),
      hiredOn: new Date().toISOString().slice(0, 10),
    };
    this.editing.set(true);
  }

  editEmployee(employee: Employee): void {
    this.employeeDraft = { ...employee };
    this.editing.set(true);
  }

  saveEmployee(): void {
    const current = this.selected();
    const request = current
      ? this.http.put<Employee>(`${this.api}/api/v1/employees/${current.id}`, this.employeeDraft, this.options())
      : this.http.post<Employee>(`${this.api}/api/v1/employees`, this.employeeDraft, this.options());
    this.loading.set(true);
    request.subscribe({
      next: (employee) => { this.editing.set(false); this.loadEmployees(0); this.selectEmployee(employee); },
      error: (error) => this.fail(error, 'Employee was not saved.'),
    });
  }

  addSalary(): void {
    const employee = this.selected();
    if (!employee || !this.amount) return;
    this.http.post<Salary>(`${this.api}/api/v1/employees/${employee.id}/salaries`, {
      amount: this.amount, currencyCode: this.currency, effectiveFrom: this.effectiveFrom,
    }, this.options()).subscribe({
      next: () => this.selectEmployee(employee),
      error: (error) => this.fail(error, 'Salary change was not saved.'),
    });
  }

  deactivate(employee: Employee): void {
    if (!confirm(`Deactivate ${employee.firstName} ${employee.lastName}?`)) return;
    this.http.delete(`${this.api}/api/v1/employees/${employee.id}`, this.options()).subscribe({
      next: () => { this.selected.set(null); this.loadEmployees(); },
      error: (error) => this.fail(error, 'Employee was not deactivated.'),
    });
  }

  exportCsv(): void {
    this.http.get(`${this.api}/api/v1/employees/export`, { ...this.options(), responseType: 'blob' }).subscribe({
      next: (blob) => this.download(blob, 'employees.csv'),
      error: (error) => this.fail(error, 'Employee export failed.'),
    });
  }

  optionLabel(options: ReferenceOption[], value: number | string): string {
    return options.find((option) => option.value === String(value))?.label ?? `Unknown (${value})`;
  }

  formatMoney(value: number, currency = 'USD'): string {
    return new Intl.NumberFormat('en-US', { style: 'currency', currency, maximumFractionDigits: 0 }).format(value || 0);
  }

  private download(blob: Blob, filename: string): void {
    const anchor = document.createElement('a');
    anchor.href = URL.createObjectURL(blob);
    anchor.download = filename;
    anchor.click();
    URL.revokeObjectURL(anchor.href);
  }

  private fail(error: HttpErrorResponse, fallback: string): void {
    this.loading.set(false);
    this.error.set(error.error?.message || fallback);
  }
}
