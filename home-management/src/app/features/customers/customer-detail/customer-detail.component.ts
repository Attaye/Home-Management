import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { CustomerService } from '../../../core/services/customer.service';
import { ReadingService } from '../../../core/services/reading.service';
import { AuthService } from '../../../core/services/auth.service'; // ✅ NEU
import { Customer, GENDER_LABELS, getFullName, getInitials } from '../../../core/models/customer.model';
import { Reading, KIND_LABELS, KIND_UNITS } from '../../../core/models/reading.model';
import { KindCountPipe } from '../../../shared/pipes/kind-count.pipe';

@Component({
  selector: 'app-customer-detail',
  standalone: true,
  imports: [CommonModule, RouterLink, KindCountPipe],
  templateUrl: './customer-detail.component.html',
  styleUrls: ['./customer-detail.component.scss'],
})
export class CustomerDetailComponent implements OnInit {
  customer: Customer | null = null;
  readings: Reading[] = [];
  loading = true;
  error = '';
  deleteConfirm = false;

  genderLabels = GENDER_LABELS;
  kindLabels   = KIND_LABELS;
  kindUnits    = KIND_UNITS;

  constructor(
      private route: ActivatedRoute,
      private router: Router,
      private customerSvc: CustomerService,
      private readingSvc: ReadingService,
      public authService: AuthService  // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    forkJoin({
      customer: this.customerSvc.getById(id),
      readings: this.readingSvc.getFiltered({ customer: id }),
    }).subscribe({
      next: ({ customer, readings }) => {
        this.customer = customer;
        this.readings = readings.sort((a, b) => b.dateOfReading.localeCompare(a.dateOfReading));
        this.loading = false;
      },
      error: () => {
        this.error = 'Kunde nicht gefunden.';
        this.loading = false;
      },
    });
  }

  getInitials(): string { return this.customer ? getInitials(this.customer) : ''; }
  getFullName(): string { return this.customer ? getFullName(this.customer) : ''; }

  formatDate(d: string | null): string {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('de-DE');
  }

  getMeterClass(kind: string): string { return kind.toLowerCase(); }

  formatNumber(n: number): string {
    return new Intl.NumberFormat('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }

  confirmDelete(): void { this.deleteConfirm = true; }
  cancelDelete(): void  { this.deleteConfirm = false; }

  doDelete(): void {
    if (!this.customer?.id) return;
    this.customerSvc.delete(this.customer.id).subscribe({
      next: () => this.router.navigate(['/kunden']),
      error: () => { this.error = 'Fehler beim Löschen des Kunden.'; this.deleteConfirm = false; },
    });
  }
}