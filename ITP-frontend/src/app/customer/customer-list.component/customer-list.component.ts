import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CustomerService } from '../../shared/services/customer.service';
import { Customer } from '../../shared/models/customer';

@Component({
  selector: 'app-customer-list',
  templateUrl: './customer-list.component.html',
  styleUrls: ['./customer-list.component.css'],
  imports: [CommonModule]
})
export class CustomerListComponent implements OnInit {

  customers: Customer[] = [];
  loading: boolean = false;

  constructor(private customerService: CustomerService) {}

  ngOnInit(): void {
    this.loadCustomers();
  }


  loadCustomers(): void {
    this.loading = true;

    this.customerService.getCustomerList().subscribe({
      next: (data: any) => {
        this.customers = data.customers; // ✅ Kundenliste extrahieren!
        this.loading = false;
      },
      error: (err) => {
        console.error("Fehler beim Laden:", err);
        this.loading = false;
      }
    });
  }


}
