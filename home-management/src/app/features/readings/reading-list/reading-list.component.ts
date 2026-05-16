import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ReadingService } from '../../../core/services/reading.service';
import { CustomerService } from '../../../core/services/customer.service';
import { AuthService } from '../../../core/services/auth.service'; // ✅ NEU
import { Reading, KindOfMeter, KIND_LABELS, KIND_UNITS, ReadingFilter } from '../../../core/models/reading.model';
import { Customer, getFullName } from '../../../core/models/customer.model';

@Component({
  selector: 'app-reading-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './reading-list.component.html',
  styleUrls: ['./reading-list.component.scss'],
})
export class ReadingListComponent implements OnInit {
  readings: Reading[] = [];
  loading = true;
  error   = '';

  filterKind:  KindOfMeter | '' = '';
  filterStart  = '';
  filterEnd    = '';
  searchQuery  = '';

  kindLabels = KIND_LABELS;
  kindUnits  = KIND_UNITS;

  kinds: Array<{ value: KindOfMeter | '', label: string }> = [
    { value: '',         label: 'Alle'    },
    { value: 'STROM',   label: 'Strom'   },
    { value: 'WASSER',  label: 'Wasser'  },
    { value: 'HEIZUNG', label: 'Heizung' },
  ];

  constructor(
      private svc:         ReadingService,
      private customerSvc: CustomerService,
      public authService:  AuthService  // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  ngOnInit(): void { this.load(); }

  load(): void {
    this.loading = true;
    const filter: ReadingFilter = {};
    if (this.filterKind)  filter.kindOfMeter = this.filterKind;
    if (this.filterStart) filter.start       = this.filterStart;
    if (this.filterEnd)   filter.end         = this.filterEnd;

    forkJoin({
      readings:  this.svc.getFiltered(filter),
      customers: this.customerSvc.getAll(),
    }).subscribe({
      next: ({ readings, customers }) => {
        const customerMap = new Map(customers.map(c => [c.id, c]));
        this.readings = readings
            .map(r => ({
              ...r,
              customer: r.customer?.id
                  ? (customerMap.get(r.customer.id) ?? r.customer)
                  : null,
            }))
            .sort((a, b) => b.dateOfReading.localeCompare(a.dateOfReading));
        this.loading = false;
      },
      error: () => {
        this.error   = 'Fehler beim Laden der Ablesungen.';
        this.loading = false;
      },
    });
  }

  get filtered(): Reading[] {
    if (!this.searchQuery) return this.readings;
    const q = this.searchQuery.toLowerCase();
    return this.readings.filter(r =>
        (r.customer ? getFullName(r.customer).toLowerCase().includes(q) : false) ||
        r.meterId.toLowerCase().includes(q)
    );
  }

  getMeterClass(kind: string): string { return kind.toLowerCase(); }

  getCustomerName(r: Reading): string {
    if (!r.customer) return '—';
    const name = getFullName(r.customer).trim();
    if (name !== '') return name;
    const id = r.customer.id;
    return id ? (id.substring(0, 8) + '...') : '—';
  }

  getInitials(r: Reading): string {
    if (!r.customer) return '?';
    const f = r.customer.firstName?.charAt(0) ?? '';
    const l = r.customer.lastName?.charAt(0)  ?? '';
    return (f || l) ? (f + l).toUpperCase() : '?';
  }

  formatNumber(n: number): string {
    return new Intl.NumberFormat('de-DE', {
      minimumFractionDigits: 2,
      maximumFractionDigits: 2,
    }).format(n);
  }
}