import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { CustomerService } from '../../../core/services/customer.service';
import { Customer, emptyCustomer } from '../../../core/models/customer.model';

@Component({
  selector: 'app-customer-form',
  standalone: true,
  imports: [CommonModule, RouterLink, ReactiveFormsModule],
  templateUrl: './customer-form.component.html',
  styleUrls: ['./customer-form.component.scss'],
})
export class CustomerFormComponent implements OnInit {
  form!: FormGroup;
  isEdit = false;
  customerId: string | null = null;
  saving = false;
  error = '';
  success = '';

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private svc: CustomerService,
  ) {}

  ngOnInit(): void {
    this.form = this.fb.group({
      firstName: ['', [Validators.required, Validators.minLength(1)]],
      lastName:  ['', [Validators.required, Validators.minLength(1)]],
      birthDate: [null],
      gender:    ['M', Validators.required],
    });

    this.customerId = this.route.snapshot.paramMap.get('id');
    this.isEdit = !!this.customerId && this.route.snapshot.url.some(s => s.path === 'bearbeiten');

    if (this.isEdit && this.customerId) {
      this.svc.getById(this.customerId).subscribe({
        next: c => this.form.patchValue(c),
        error: () => { this.error = 'Kunde nicht gefunden.'; },
      });
    }
  }

  submit(): void {
    if (this.form.invalid) { this.form.markAllAsTouched(); return; }
    this.saving = true;
    this.error = '';

    const customer: Customer = {
      id: this.isEdit ? this.customerId : null,
      ...this.form.value,
      birthDate: this.form.value.birthDate || null,
    };

    if (this.isEdit) {
      this.svc.update(customer).subscribe({
        next: () => this.router.navigate(['/kunden', this.customerId]),
        error: () => { this.error = 'Fehler beim Speichern. Bitte prüfe die Eingaben.'; this.saving = false; },
      });
    } else {
      this.svc.create(customer).subscribe({
        next: (created: Customer) => this.router.navigate(['/kunden', created.id]),
        error: () => { this.error = 'Fehler beim Speichern. Bitte prüfe die Eingaben.'; this.saving = false; },
      });
    }
  }

  get f() { return this.form.controls; }

  isInvalid(field: string): boolean {
    const c = this.form.get(field);
    return !!(c && c.invalid && c.touched);
  }
}
