import { CommonModule, DecimalPipe } from '@angular/common';
import { Component, Input } from '@angular/core';
import { Summary } from '../models';

@Component({
  selector: 'app-overview',
  standalone: true,
  imports: [CommonModule, DecimalPipe],
  templateUrl: './overview.component.html',
  styleUrl: './overview.component.scss',
})
export class OverviewComponent {
  @Input({ required: true }) summary!: Summary;

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
