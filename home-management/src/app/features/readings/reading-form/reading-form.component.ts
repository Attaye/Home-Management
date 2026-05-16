import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { ReadingService } from '../../../core/services/reading.service';
import { CustomerService } from '../../../core/services/customer.service';
import { Reading, emptyReading } from '../../../core/models/reading.model';
import { Customer, getFullName } from '../../../core/models/customer.model';
import { forkJoin } from 'rxjs';

@Component({
  selector: 'app-reading-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './reading-form.component.html',
  styleUrls: ['./reading-form.component.scss'],
})
export class ReadingFormComponent implements OnInit {
  form!: FormGroup;
  isEdit      = false;
  readingId: string | null = null;
  saving      = false;
  loading     = true;
  error       = '';
  customers:  Customer[] = [];

  constructor(
      private fb:         FormBuilder,
      private route:      ActivatedRoute,
      private router:     Router,
      private readingSvc: ReadingService,
      private customerSvc: CustomerService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      customerId:    ['', Validators.required],
      dateOfReading: [new Date().toISOString().split('T')[0], Validators.required],
      kindOfMeter:   ['STROM', Validators.required],
      meterCount:    [0, [Validators.required, Validators.min(0)]],
      meterId:       ['', Validators.required],
      substitute:    [false],
      comment:       [''],
    });

    this.readingId = this.route.snapshot.paramMap.get('id');
    this.isEdit    = !!this.readingId &&
        this.route.snapshot.url.some(s => s.path === 'bearbeiten');

    // Kunden laden + ggf. bestehende Ablesung laden
    if (this.isEdit && this.readingId) {
      forkJoin({
        customers: this.customerSvc.getAll(),
        reading:   this.readingSvc.getById(this.readingId),
      }).subscribe({
        next: ({ customers, reading }) => {
          this.customers = customers;
          // ✅ FIX: Kundenzuordnung korrekt vorausfüllen
          this.form.patchValue({
            customerId:    reading.customer?.id ?? '',
            dateOfReading: reading.dateOfReading,
            kindOfMeter:   reading.kindOfMeter,
            meterCount:    reading.meterCount,
            meterId:       reading.meterId,
            substitute:    reading.substitute,
            comment:       reading.comment ?? '',
          });
          this.loading = false;
        },
        error: () => { this.error = 'Ablesung nicht gefunden.'; this.loading = false; },
      });
    } else {
      // Kunden laden + Query-Param für Kunden-Vorauswahl
      this.customerSvc.getAll().subscribe({
        next: cs => {
          this.customers = cs;
          const preCustomer = this.route.snapshot.queryParamMap.get('customer');
          if (preCustomer) this.form.patchValue({ customerId: preCustomer });
          this.loading = false;
        },
        error: () => { this.loading = false; },
      });
    }
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error  = '';

    const val             = this.form.value;
    const selectedCustomer = this.customers.find(c => c.id === val.customerId) ?? null;

    const reading: Reading = {
      id:            this.isEdit ? this.readingId : null,
      customer:      selectedCustomer,
      dateOfReading: val.dateOfReading,
      kindOfMeter:   val.kindOfMeter,
      meterCount:    Number(val.meterCount),
      meterId:       val.meterId,
      substitute:    val.substitute ?? false,
      comment:       val.comment || null,
    };

    if (this.isEdit) {
      this.readingSvc.update(reading).subscribe({
        next: () => this.router.navigate(['/ablesungen', this.readingId]),
        error: () => { this.error = 'Fehler beim Speichern.'; this.saving = false; },
      });
    } else {
      this.readingSvc.create(reading).subscribe({
        next: (created: Reading) => this.router.navigate(['/ablesungen', created.id]),
        error: () => { this.error = 'Fehler beim Speichern.'; this.saving = false; },
      });
    }
  }

  getCustomerName(c: Customer): string { return getFullName(c); }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c?.invalid && c?.touched);
  }
}