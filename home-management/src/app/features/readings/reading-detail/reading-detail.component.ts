import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReadingService } from '../../../core/services/reading.service';
import { AuthService } from '../../../core/services/auth.service'; // ✅ NEU
import { Reading, KIND_LABELS, KIND_UNITS } from '../../../core/models/reading.model';
import { getFullName, getInitials } from '../../../core/models/customer.model';

@Component({
  selector: 'app-reading-detail',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './reading-detail.component.html',
  styleUrls: ['./reading-detail.component.scss'],
})
export class ReadingDetailComponent implements OnInit {
  reading: Reading | null = null;
  loading = true;
  error = '';
  deleteConfirm = false;

  kindLabels = KIND_LABELS;
  kindUnits  = KIND_UNITS;

  constructor(
      private route: ActivatedRoute,
      private router: Router,
      private svc: ReadingService,
      public authService: AuthService  // ✅ NEU: public damit HTML darauf zugreifen kann
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id')!;
    this.svc.getById(id).subscribe({
      next: r  => { this.reading = r; this.loading = false; },
      error: () => { this.error = 'Ablesung nicht gefunden.'; this.loading = false; },
    });
  }

  getMeterClass(kind: string): string { return kind.toLowerCase(); }

  getCustomerName(): string {
    return this.reading?.customer ? getFullName(this.reading.customer) : '—';
  }
  getCustomerInitials(): string {
    return this.reading?.customer ? getInitials(this.reading.customer) : '?';
  }

  formatNumber(n: number): string {
    return new Intl.NumberFormat('de-DE', { minimumFractionDigits: 2, maximumFractionDigits: 2 }).format(n);
  }

  confirmDelete(): void  { this.deleteConfirm = true; }
  cancelDelete(): void   { this.deleteConfirm = false; }

  doDelete(): void {
    if (!this.reading?.id) return;
    this.svc.delete(this.reading.id).subscribe({
      next: () => this.router.navigate(['/ablesungen']),
      error: () => { this.error = 'Fehler beim Löschen.'; this.deleteConfirm = false; },
    });
  }
}