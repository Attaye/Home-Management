import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CustomerService } from '../../core/services/customer.service';
import { ReadingService } from '../../core/services/reading.service';
import { AuthService } from '../../core/services/auth.service'; // ✅ NEU
import { Customer, getFullName } from '../../core/models/customer.model';
import { Reading, KIND_LABELS, KIND_UNITS } from '../../core/models/reading.model';

interface StatCard { label: string; value: number | string; icon: string; color: string; }
interface CustomerStat { customer: Customer; count: number; }

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss'],
})
export class DashboardComponent implements OnInit {
  loading = true;
  error   = '';

  stats:          StatCard[]     = [];
  recentReadings: Reading[]      = [];
  customerStats:  CustomerStat[] = [];

  kindLabels = KIND_LABELS;
  kindUnits  = KIND_UNITS;

  constructor(
      private customerSvc: CustomerService,
      private readingSvc:  ReadingService,
      public authService:  AuthService   // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  ngOnInit(): void {
    forkJoin({
      customers: this.customerSvc.getAll(),
      readings:  this.readingSvc.getFiltered(),
    }).subscribe({
      next: ({ customers, readings }) => {
        const customerMap = new Map(
            customers.map(c => [c.id?.toLowerCase() ?? '', c])
        );

        const enriched = readings.map(r => ({
          ...r,
          customer: r.customer?.id
              ? (customerMap.get(r.customer.id.toLowerCase()) ?? r.customer)
              : null,
        }));

        this.buildStats(customers, enriched);
        this.recentReadings = enriched
            .slice()
            .sort((a, b) => b.dateOfReading.localeCompare(a.dateOfReading))
            .slice(0, 6);
        this.buildCustomerStats(customers, enriched);
        this.loading = false;
      },
      error: () => {
        this.error   = 'Verbindung zum Server fehlgeschlagen. Ist das Backend gestartet?';
        this.loading = false;
      },
    });
  }

  private buildStats(customers: Customer[], readings: Reading[]): void {
    const estimates   = readings.filter(r => r.substitute).length;
    const today       = new Date().toISOString().split('T')[0];
    const activeToday = readings.filter(r => r.dateOfReading === today).length;

    this.stats = [
      { label: 'Kunden gesamt', value: customers.length, icon: '👥', color: '#3b82f6' },
      { label: 'Ablesungen',    value: readings.length,  icon: '📊', color: '#22c55e' },
      { label: 'Schätzungen',   value: estimates,        icon: '📋', color: '#f59e0b' },
      { label: 'Aktiv heute',   value: activeToday,      icon: '📅', color: '#a78bfa' },
    ];
  }

  private buildCustomerStats(customers: Customer[], readings: Reading[]): void {
    this.customerStats = customers
        .map(c => ({
          customer: c,
          count: readings.filter(r => r.customer?.id?.toLowerCase() === c.id?.toLowerCase()).length,
        }))
        .sort((a, b) => b.count - a.count)
        .slice(0, 5);
  }

  getInitials(c: Customer): string {
    return (c.firstName.charAt(0) + c.lastName.charAt(0)).toUpperCase();
  }

  getFullName(c: Customer): string { return getFullName(c); }

  getMeterClass(kind: string): string { return kind.toLowerCase(); }

  getCustomerName(r: Reading): string {
    if (!r.customer) return '—';
    const name = getFullName(r.customer).trim();
    return name || '—';
  }

  getReadingInitials(r: Reading): string {
    if (!r.customer?.firstName) return '?';
    return (r.customer.firstName.charAt(0) + r.customer.lastName.charAt(0)).toUpperCase();
  }

  formatNumber(n: number): string {
    return new Intl.NumberFormat('de-DE', {
      minimumFractionDigits: 2, maximumFractionDigits: 2,
    }).format(n);
  }
}