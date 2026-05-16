import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { AuthService } from '../../../core/services/auth.service'; // ✅ NEU
import { Customer, GENDER_LABELS, getFullName, getInitials } from '../../../core/models/customer.model';

@Component({
  selector: 'app-customer-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule],
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.scss'],
})
export class CustomerListComponent implements OnInit {
  customers: Customer[] = [];
  filtered: Customer[] = [];
  loading = true;
  error = '';
  searchQuery = '';

  genderLabels = GENDER_LABELS;

  constructor(
      private svc: CustomerService,
      public authService: AuthService  // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.svc.getAll().subscribe({
      next: cs => {
        this.customers = cs;
        this.applyFilter();
        this.loading = false;
      },
      error: () => {
        this.error = 'Fehler beim Laden der Kunden.';
        this.loading = false;
      },
    });
  }

  applyFilter(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filtered = q
        ? this.customers.filter(c =>
            getFullName(c).toLowerCase().includes(q) ||
            (c.id ?? '').toLowerCase().includes(q)
        )
        : [...this.customers];
  }

  getInitials(c: Customer): string { return getInitials(c); }
  getFullName(c: Customer): string { return getFullName(c); }

  formatDate(d: string | null): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('de-DE');
  }
}