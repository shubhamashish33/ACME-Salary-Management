import { CommonModule, DecimalPipe } from '@angular/common';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Component, EventEmitter, Input, OnInit, Output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { DashboardFilters, ReferenceOption, Summary } from '../models';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, DecimalPipe, FormsModule],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
})
export class OverviewComponent implements OnInit {
  @Input({ required: true }) summary!: Summary;
  @Input({ required: true }) api = '';
  @Input({ required: true }) auth!: HttpHeaders;
  @Input() filters: DashboardFilters = {};
  @Output() filtersChange = new EventEmitter<DashboardFilters>();
  countries = signal<ReferenceOption[]>([]);
  departments = signal<ReferenceOption[]>([]);
  levels = signal<ReferenceOption[]>([]);
  selectedCountry = '';
  selectedDepartment = '';
  selectedLevel = '';
  selectedGender = '';

  constructor(private http: HttpClient) {}

  ngOnInit(): void {
    this.selectedCountry = this.filters.country ?? '';
    this.selectedDepartment = this.filters.departmentId?.toString() ?? '';
    this.selectedLevel = this.filters.jobLevelId?.toString() ?? '';
    this.selectedGender = this.filters.gender ?? '';
    const options = { headers: this.auth };
    forkJoin({
      countries: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/countries`, options),
      departments: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/departments`, options),
      levels: this.http.get<ReferenceOption[]>(`${this.api}/api/v1/reference/levels`, options),
    }).subscribe(({ countries, departments, levels }) => {
      this.countries.set(countries);
      this.departments.set(departments);
      this.levels.set(levels);
    });
  }

  applyFilters(): void {
    this.filtersChange.emit({
      country: this.selectedCountry || undefined,
      departmentId: this.selectedDepartment ? Number(this.selectedDepartment) : undefined,
      jobLevelId: this.selectedLevel ? Number(this.selectedLevel) : undefined,
      gender: this.selectedGender || undefined,
    });
  }

  clearFilters(): void {
    this.selectedCountry = '';
    this.selectedDepartment = '';
    this.selectedLevel = '';
    this.selectedGender = '';
    this.filtersChange.emit({});
  }

  formatMoney(value: number): string {
    return new Intl.NumberFormat('en-US', {
      style: 'currency',
      currency: this.summary.reportingCurrency,
      maximumFractionDigits: 0,
    }).format(value || 0);
  }

  barWidth(value: number): number {
    const maximum = Math.max(...this.summary.byCountry.map((row) => row.averageSalary), 1);
    return (value / maximum) * 100;
  }
}
